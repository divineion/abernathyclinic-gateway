package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@Profile("dev")
public class DevSecurityConfiguration {

	@Bean
	SecurityWebFilterChain filterChain(ServerHttpSecurity serverHttpSecurity) throws Exception {   	
    	return serverHttpSecurity
        		// configurer les autorisations
        		.authorizeExchange(exchange -> exchange
        			.pathMatchers("/login").permitAll()
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
}
