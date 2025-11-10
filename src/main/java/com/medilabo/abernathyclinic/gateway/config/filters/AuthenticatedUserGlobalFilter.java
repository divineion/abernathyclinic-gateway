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

public class AuthenticatedUserGlobalFilter implements GlobalFilter, Ordered {
		
	// GatewayFilterChain = s'occupe du routage et des filtres de passage 
	// donc pour les headers c'est là que ça se passe
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
	    Mono<SecurityContext> context = ReactiveSecurityContextHolder.getContext();
	    
	    // 1: recup l'authentification
	    // le contexte est stocké dans le flux réactif lui-même, donc Mono<SecurityContext>
	        return context.map(securityContext -> (Authentication) securityContext.getAuthentication())
	        
	        // 2. l'utilisateur est authentifié
	        .flatMap(authentication -> {
	        	// 3. récup le user
	            Object principal = authentication.getPrincipal();

	            // 3. avec son rôle (synchrone)
	            String role = authentication.getAuthorities().stream()
	                .map(GrantedAuthority::getAuthority)
	                .collect(Collectors.joining(",")); 
	            
	            // 3 : et son id
	            String userId = null;

	            if (principal instanceof AppUser appUser) { 
	                userId = String.valueOf(appUser.getId());
                }

	            // 4: mutation de l'exchange : 
		            ServerWebExchange mutatedExchange = exchange.mutate()
		    	            // ajout des en-têtes à la requête
		                .request(exchange.getRequest().mutate()
		                    .header("X-Auth-User-Roles", role)
		                    .header("X-Auth-User-Id", userId)
		                    .build())
		                .build();
		          
		            // 5 : retourner le Mono<Void> en poursuivant la chaîne avec l'exchange muté
	            	return chain.filter(mutatedExchange);
	        })
	        
	        // 5. si mono vide, l'utilisateur n'est pas authentifié
	        // donc retourner un Mono<Void> avec l'exchange non modifié
	        .switchIfEmpty(chain.filter(exchange)); 
	}

	// the filter with the highest precedence is the first in the “pre”-phase and the last in the “post”-phase
	@Override
	public int getOrder() {
		return -1;
	}
}