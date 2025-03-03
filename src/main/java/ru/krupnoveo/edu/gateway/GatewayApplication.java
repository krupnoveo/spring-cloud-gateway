package ru.krupnoveo.edu.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import ru.krupnoveo.edu.gateway.config.RoutesRolesConfig;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(RoutesRolesConfig.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
