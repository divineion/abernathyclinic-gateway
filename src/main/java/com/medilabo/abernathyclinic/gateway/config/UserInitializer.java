package com.medilabo.abernathyclinic.gateway.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.vault.support.VaultResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

/**
 * This configuration class initializes in-memory demo users and stores their credentials
 *  in Vault server at application startup. 
 */
@Profile({"dev", "docker"})
@Configuration
public class UserInitializer {
	private static final Logger logger = LogManager.getLogger(UserInitializer.class);

    private final VaultSecretWriter vaultSecretWriter;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;
		
	public UserInitializer(PasswordEncoder passwordEncoder, VaultSecretWriter vaultSecretWriter, ObjectMapper objectMapper) {
		this.passwordEncoder = passwordEncoder;
		this.vaultSecretWriter = vaultSecretWriter;
		this.objectMapper = objectMapper;
	}
	
	@Value("${ORGANIZER1_PASSWORD}")
	private String organizer1Password;
	
	@Value("${ORGANIZER2_PASSWORD}")
	private String organizer2Password;
	
	@Value("${ORGANIZER3_PASSWORD}")
	private String organizer3Password;
	
	@Value("${DOCTOR1_PASSWORD}")
	private String doctor1Password;
	
	@Value("${DOCTOR2_PASSWORD}")
	private String doctor2Password;
	
	@Value("${DOCTOR3_PASSWORD}")
	private String doctor3Password;
	
	@Value("${vault.kv.path}")
	private String usersSecretBasePath;

	@Bean
	CustomMapReactiveUserDetailsService initDemoUsers() throws JsonProcessingException {	
		List<AppUser> users = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
	
		
		AppUser organizer1 = new AppUser(
			1,	
			UUID.randomUUID(),
			"organizer1",
			passwordEncoder.encode(organizer1Password),
			List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")),
			now,
			now
		);
		users.add(organizer1);
		
		AppUser organizer2 = new AppUser(
			2,	
			UUID.randomUUID(),
			"organizer2",
			passwordEncoder.encode(organizer2Password),
			List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")),
			LocalDateTime.now(),
			LocalDateTime.now()
		);
		users.add(organizer2);
		
		AppUser organizer3 = new AppUser(
			3,	
			UUID.randomUUID(),
			"organizer3",
			passwordEncoder.encode(organizer3Password),
			List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")),
			LocalDateTime.now(),
			LocalDateTime.now()
		);
		users.add(organizer3);
		
		AppUser doctor1 = new AppUser(
			4,	
			UUID.randomUUID(),
			"doctor1",
			passwordEncoder.encode(doctor1Password),
			List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")),
			LocalDateTime.now(),
			LocalDateTime.now()
		);
		users.add(doctor1);
		
		AppUser doctor2 = new AppUser(
			5,	
			UUID.randomUUID(),
			"doctor2",
			passwordEncoder.encode(doctor2Password),
			List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")),
			LocalDateTime.now(),
			LocalDateTime.now()
		);
		users.add(doctor2);
		
		AppUser doctor3 = new AppUser(
			6,	
			UUID.randomUUID(),
			"doctor3",
			passwordEncoder.encode(doctor3Password),
			List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")),
			LocalDateTime.now(),
			LocalDateTime.now()
		);
		users.add(doctor3);
		
	
		for (AppUser user : users) {
			storeUserSecrets(user).subscribe();
		}
				
		return new CustomMapReactiveUserDetailsService(users);
	}

	private Mono<Void> storeUserSecrets(AppUser user) throws JsonMappingException, JsonProcessingException {
		Map<String, Object> secret;
		
		secret = objectMapper.convertValue(user, new TypeReference<Map<String, Object>>(){});
				
		Mono<VaultResponse> writeUserSecret = vaultSecretWriter.writeSecret(usersSecretBasePath + user.getUsername(), secret);
		
		return writeUserSecret
			.doOnSuccess(_ -> 
				logger.info("User with username " + user.getUsername() + " has been saved"))
			.doOnError(error -> logger.error(error.getMessage()))
			.then();		
				
	}
}
