package com.medilabo.abernathyclinic.gateway.config.filters;


import java.util.stream.Collectors;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;

import com.medilabo.abernathyclinic.gateway.config.AppUser;

import reactor.core.publisher.Mono;

/**
 * Propagate authenticated user information to downstream requests.
 * 
 * For every authenticated request through the gateway:
 * Extracts user roles and ID from Spring Security context
 * Adds headers to the request forwarded to microservices:
 *    - `X-Auth-User-Roles`: role
 *    - `X-Auth-User-Id`: user ID from AppUser
 * Forwards unauthenticated requests without modification.
 * 
 * <p>High priority (order -1) to run early in filter chain.</p>
 * 
 * @see GlobalFilter
 * @see Ordered
 */
public class AuthenticatedUserGlobalFilter implements GlobalFilter, Ordered {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
	    Mono<SecurityContext> context = ReactiveSecurityContextHolder.getContext();
	    
	        return context.map(securityContext -> (Authentication) securityContext.getAuthentication())
	        
	        .flatMap(authentication -> {
	            Object principal = authentication.getPrincipal();

	            String role = authentication.getAuthorities().stream()
	                .map(GrantedAuthority::getAuthority)
	                .collect(Collectors.joining(",")); 
	            
	            String userId = null;

	            if (principal instanceof AppUser appUser) { 
	                userId = String.valueOf(appUser.getId());
                }

		            ServerWebExchange mutatedExchange = exchange.mutate()
		                .request(exchange.getRequest().mutate()
		                    .header("X-Auth-User-Roles", role)
		                    .header("X-Auth-User-Id", userId)
		                    .build())
		                .build();
		          
	            	return chain.filter(mutatedExchange);
	        })
	        
	        .switchIfEmpty(chain.filter(exchange)); 
	}

	@Override
	public int getOrder() {
		return -1;
	}
}