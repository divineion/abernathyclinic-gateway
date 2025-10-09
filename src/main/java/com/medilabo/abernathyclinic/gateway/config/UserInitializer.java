package com.medilabo.abernathyclinic.gateway.config;


import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * This configuration class initializes in-memory demo users and stores their credentials
 *  in Vault server at application startup. 
 */
@Profile("dev")
@Configuration
public class UserInitializer {

    private final VaultSecretWriter vaultSecretWriter;
	private final PasswordEncoder passwordEncoder;
		
	public UserInitializer(PasswordEncoder passwordEncoder, VaultSecretWriter vaultSecretWriter) {
		this.passwordEncoder = passwordEncoder;
		this.vaultSecretWriter = vaultSecretWriter;
	}
	
	@Value("${ORGANIZER1_PASSWORD}")
	private String organizer1Password;
	
	@Value("${ORGANIZER2_PASSWORD}")
	private String organizer2Password;
	
	@Value("${spring.cloud.vault.kv.path}")
	private String usersSecretBasePath;

	@Bean
	UserDetailsService users() {	
			//  create users
		UserDetails organizer1 = User.builder()
			.username("organizer1")
			.password(passwordEncoder.encode(organizer1Password))
			.roles("ORGANIZER")
			.build();
		
		
		UserDetails organizer2 = User.builder()
			.username("organizer2")
			.password(passwordEncoder.encode(organizer2Password))
			.roles("ORGANIZER")
			.build();
		
		Map<String, String> secret1 = Map.of("password", organizer1.getPassword());
		Map<String, String> secret2 = Map.of("password", organizer2.getPassword());
				
		// write secrets into Vault backend storage
		vaultSecretWriter.writeSecret(usersSecretBasePath+"organizer1", secret1);
		vaultSecretWriter.writeSecret(usersSecretBasePath+"organizer2", secret2);
		
		return new InMemoryUserDetailsManager(organizer1, organizer2);
	}
}
