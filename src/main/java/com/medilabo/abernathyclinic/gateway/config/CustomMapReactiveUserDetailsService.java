package com.medilabo.abernathyclinic.gateway.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import reactor.core.publisher.Mono;

public class CustomMapReactiveUserDetailsService implements ReactiveUserDetailsService {
	Map<String, AppUser> users;
	
	public CustomMapReactiveUserDetailsService(List<AppUser> appUsers) {
		this.users = appUsers.stream()
				.collect(Collectors.toMap(AppUser::getUsername, Function.identity()));
	}
	
	@Override
	public Mono<UserDetails> findByUsername(String username) {
		AppUser user = users.get(username.toLowerCase());
		
		if (user == null) {
			return Mono.error(new UsernameNotFoundException("user " + username + " not found"));
		}
		
		return Mono.just(user);
	}
}
