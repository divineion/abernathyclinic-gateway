package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.medilabo.abernathyclinic.gateway.config.filters.AuthenticatedUserGlobalFilter;

/**
 * Security configuration used in "dev" and "docker" profiles.
 *
 * This configuration protects sensible routes behind Basic Authentication. 
 * It also configures CORS for local frontend development.
 */
@Configuration
@Profile({"dev", "docker"})
public class DevSecurityConfiguration {

	@Bean
	SecurityWebFilterChain filterChain(ServerHttpSecurity serverHttpSecurity) throws Exception {   	
    	return serverHttpSecurity
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchange -> exchange
	        		.pathMatchers("/user").permitAll()
	                .pathMatchers(HttpMethod.OPTIONS, "/user", "/patients", "/patient/**", "/notes/**", "/note/**", "/report/**").permitAll()
	                .anyExchange().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
    }
    
    @Bean
    AuthenticatedUserGlobalFilter authenticatedUserFilter() {
    	return new AuthenticatedUserGlobalFilter();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.addAllowedOrigin("http://localhost:5174");
        corsConfig.addAllowedMethod("GET");
        corsConfig.addAllowedMethod("POST");
        corsConfig.addAllowedMethod("PATCH");
        corsConfig.addAllowedMethod("OPTIONS");
        corsConfig.addAllowedHeader("*");
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
