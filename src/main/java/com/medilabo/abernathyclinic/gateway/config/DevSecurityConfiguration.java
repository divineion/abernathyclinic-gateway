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

import com.medilabo.abernathyclinic.gateway.config.filters.AuthenticatedUserGlobalFilter;


@Configuration
@Profile("dev")
public class DevSecurityConfiguration {

	@Bean
	SecurityWebFilterChain filterChain(ServerHttpSecurity serverHttpSecurity) throws Exception {   	
    	return serverHttpSecurity
                // configurer les autorisations
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchange -> exchange
	                // authoriser les requêtes preflights sans authentification
	                // sans quoi elle seront bloquées par Spring Security au moment de vérifier la politique CORS
                		// autoriser la requêet au endpoint user -> set le login en front
                		// puis envoyer le token avec chaque requête
                		// en autorisant toutes requêtes OPTIONS 
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
}
