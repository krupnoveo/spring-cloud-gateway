package ru.krupnoveo.edu.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.krupnoveo.edu.gateway.config.RoutesRolesConfig;
import ru.krupnoveo.edu.gateway.dto.UserDetailsResponse;
import ru.krupnoveo.edu.gateway.exception.UnathourizedException;

import java.util.List;
import java.util.UUID;

@Component
public class ValidateRoleFilter extends AbstractGatewayFilterFactory<ValidateRoleFilter.Config> {
    private static final RuntimeException UNATHOURIZED_EXCEPTION = new UnathourizedException(
            "Access denied! Don't have permission to access this resource"
    );

    private final RoutesRolesConfig routesRolesConfig;
    private final WebClient client;
    private final String baseUrl;

    public ValidateRoleFilter(
            @Autowired RoutesRolesConfig routesRolesConfig,
            @Autowired WebClient.Builder client,
            @Value("${user.service.name}") String userServiceName
    ) {
        super(Config.class);
        this.routesRolesConfig = routesRolesConfig;
        this.client = client.build();
        this.baseUrl = "http://" + userServiceName;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            var requiredRolesAndToken = getRequiredRolesAndToken(exchange);
            if (requiredRolesAndToken == null) {
                return chain.filter(exchange);
            }
            String token = requiredRolesAndToken.token;
            List<String> finalRequiredRoles = requiredRolesAndToken.requiredRoles;
            return checkRole(exchange, chain, token, finalRequiredRoles);
        });
    }

    private Mono<Void> checkRole(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String token,
            List<String> finalRequiredRoles
    ) {
        return client
                .get()
                .uri(baseUrl + "/auth/verify?token={token}", token)
                .retrieve()
                .bodyToMono(UserDetailsResponse.class)
                .flatMap(user -> {
                    var userRoles = user.roles().stream().map(a -> {
                        a = a.toLowerCase().substring(5);
                        return a;
                    }).toList();
                    if (finalRequiredRoles.contains(userRoles.getFirst())) {
                        return chain.filter(exchange);
                    }
                    return Mono.error(UNATHOURIZED_EXCEPTION);
                })
                .onErrorResume(throwable -> {
                    throw UNATHOURIZED_EXCEPTION;
                });
    }

    public static class Config {
    }

    private RequiredRolesAndToken getRequiredRolesAndToken(
            ServerWebExchange exchange
    ) {
        String path = exchange.getRequest().getURI().getPath();
        String[] parts = path.split("/");
        if (checkPath(parts)) {
            parts[2] = "{id}";
            path = String.join("/", parts);
        }
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        var routes = routesRolesConfig.getServices();
        List<String> requiredRoles = null;
        for (var entry : routes.entrySet()) {
            if (entry.getValue().containsKey(path)) {
                requiredRoles = entry.getValue().get(path);
            }
        }
        if (requiredRoles == null) {
            throw UNATHOURIZED_EXCEPTION;
        }
        if (requiredRoles.getFirst().equals("none")) {
            return null;
        }
        if (authHeader == null || !authHeader.getFirst().startsWith("Bearer ")) {
            throw UNATHOURIZED_EXCEPTION;
        }
        return new RequiredRolesAndToken(
                authHeader.getFirst().replace("Bearer ", ""),
                requiredRoles
        );
    }

    private static boolean checkPath(String[] parts) {
        try {
            UUID.fromString(parts[2]);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    private record RequiredRolesAndToken(String token, List<String> requiredRoles) {}
}