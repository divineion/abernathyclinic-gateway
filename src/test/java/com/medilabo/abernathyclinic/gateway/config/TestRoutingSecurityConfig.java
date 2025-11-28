package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@TestConfiguration
public class TestRoutingSecurityConfig {
	@Bean
    RouteLocator testRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
    		// la route exacte en priorité
            .route("patient_create_route", r -> r.path("/patient")
        		.filters(f -> f.rewritePath("/patient", "/api/patient"))
        		.uri("http://localhost:8097"))
    
    		.route("patient_route", r -> r.path("/patient/**")
	            .filters(f -> f.rewritePath("/patient/(?<segment>.*)", "/api/patient/${segment}")) 
	            .uri("http://localhost:8097"))

            .route("notes_route", r -> r.path("/notes/**")
                .filters(f -> f.rewritePath("/notes/(?<segment>.*)", "/api/notes/${segment}"))  
                .uri("http://localhost:8098"))
                
            .route("report_route", r -> r.path("/report/**")
                .filters(f -> f.rewritePath("/report/(?<segment>.*)", "/api/report/${segment}"))
                .uri("http://localhost:8099"))
            .build();
    }
	
    @Bean
    SecurityWebFilterChain disableSecurity(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                   .authorizeExchange(auth -> auth.anyExchange().permitAll())
                   .build();
    }
}
