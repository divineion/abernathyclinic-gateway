package com.medilabo.abernathyclinic.gateway.config;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AppUser implements UserDetails {
	private static final long serialVersionUID = 7089376405301370171L;
	private final long id;
	private final UUID uuid;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    
    private final LocalDateTime createdAt;
    
    private final LocalDateTime updatedAt;
	
	public AppUser(long id, UUID uuid, String username, String password, Collection<? extends GrantedAuthority> authorities, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.uuid = uuid;
		this.username = username;
		this.password = password;
		this.authorities = authorities;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public long getId() {
		return id;
	}

	public UUID getUuid() {
		return uuid;
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
