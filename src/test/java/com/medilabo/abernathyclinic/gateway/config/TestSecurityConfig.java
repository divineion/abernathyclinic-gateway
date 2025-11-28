package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    SecurityWebFilterChain securityForTests(ServerHttpSecurity http, ReactiveUserDetailsService userDetailsService) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth
                .pathMatchers("/user").authenticated()
                .anyExchange().permitAll()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
    }
}

