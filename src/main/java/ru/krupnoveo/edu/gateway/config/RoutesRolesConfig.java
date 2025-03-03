package ru.krupnoveo.edu.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.krupnoveo.edu.gateway.models.Service;
import ru.krupnoveo.edu.gateway.models.ServiceRoute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "routes-config")
@Configuration
public class RoutesRolesConfig {
    private final Map<String, Map<String, List<String>>> services = new HashMap<>();

    public void setServices(List<Service> services) {
        for (Service service : services) {
            Map<String, List<String>> routes = new HashMap<>();
            for (ServiceRoute route : service.routes()) {
                var roles = Arrays.stream(route.roles().split(",")).map(String::trim).toList();
                routes.put(route.path(), roles);
            }
            this.services.put(service.name(), routes);
        }
    }

    public Map<String, Map<String, List<String>>> getServices() {
        return services;
    }
}
