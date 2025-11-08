package com.bits.gateway_server.config;

import com.bits.gateway_server.filter.AwsCognitoAuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@AllArgsConstructor
public class GatewayConfig {
    private final AwsCognitoAuthFilter cognitoAuthFilter;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("track_route", r -> r
                .path("/api/delivery/track/**")
                .filters(f -> f.rewritePath("/api/delivery/track/(?<remaining>.*)", "/api/delivery/track/${remaining}"))
                .uri("lb://delivery-service")
            )

            .route("delivery_protected", r -> r
                .path("/api/delivery/**")
                .filters(f -> f
                    .rewritePath("/api/delivery/(?<segment>.*)", "/api/delivery/${segment}")
                    .filter(cognitoAuthFilter)
                    .circuitBreaker(cb -> cb
                        .setName("deliveryServiceCB")
                        .setFallbackUri("forward:/fallback/delivery")
                    )
                )
                .uri("lb://delivery-service")
            )

            .route("fallback_route", r -> r
                .path("/fallback/**")
                .uri("http://localhost:8088")
            )
            .build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("*"));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }
}
