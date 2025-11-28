package com.medilabo.abernathyclinic.gateway.it;


import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.vault.support.VaultResponse;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import com.medilabo.abernathyclinic.gateway.config.VaultSecretReader;
import com.medilabo.abernathyclinic.gateway.config.VaultSecretWriter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Integration tests for Vault read/write operations using {@link Testcontainers}.
 *
 * <p>
 * Starts a Vault container and injects its host, port, and tokens into Spring context.
 * Tests that {@link VaultSecretReader} can read secrets correctly and 
 * {@link VaultSecretWriter} can write secrets and verify them immediately.
 * Uses StepVerifier to validate reactive responses.
 * </p>
 */

@Profile("test")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class VaultTestContainerIT {
	
	private static final String VAULT_IMAGE_VERSION = "hashicorp/vault:1.13";
	
	private static String TEST_TOKEN = "TEST_TOKEN";
	private static String PATH = "secret/abernathyclinic/test/users/";
	
	private static String USERNAME = "Testusername";
	
	private static String CLI_USERNAME = "Testcli";
	private final static String CLI_PASSWORD = "CLIpassword123";
	
	private static final String disableSecretEngine = "secrets disable secret";
	private static final String reinitSecretEngine =  "secrets enable -version=1 -path=secret kv";
	
	private static final String writeTestSecret = "kv put " + PATH + CLI_USERNAME + " password=" + CLI_PASSWORD + " username=" + CLI_USERNAME;
	
	
	@Container
	public static final VaultContainer<?> vaultContainer = new VaultContainer<>(VAULT_IMAGE_VERSION)
    .withVaultToken(TEST_TOKEN)
    .withInitCommand(
    		disableSecretEngine,
    		reinitSecretEngine,
    		writeTestSecret
    );
	
	@Autowired
	private VaultSecretReader vaultSecretReader;
	
	@Autowired
	private VaultSecretWriter vaultSecretWriter;
	
	@DynamicPropertySource
	static void vaultProperties(DynamicPropertyRegistry registry) {
		registry.add("vault.host", vaultContainer::getHost);
		registry.add("vault.port", vaultContainer::getFirstMappedPort);
		registry.add("vault.token.users.read", () -> TEST_TOKEN);
		registry.add("vault.token.users.write", () -> TEST_TOKEN);
		registry.add("vault.backend.kv.version", () -> 1);
	}
	
	@Test
	public void readSecretTest() {
		Map<String, Object> expectedData = Map.of("username", CLI_USERNAME, "password", CLI_PASSWORD);
		
		Mono<Map<String, Object>> data = vaultSecretReader.readSecret(PATH + CLI_USERNAME)
		.map(VaultResponse::getData);
		
		StepVerifier.create(data)
			.expectNext(expectedData)
			.verifyComplete();
	}
	
	@Test
	public void writeSecretTest() {
        Map<String, Object> expectedData = Map.of("username", USERNAME, "password", "testpassword");
        
        Mono<Map<String, Object>> data = vaultSecretWriter.writeSecret(PATH + USERNAME, expectedData)
        		.map(VaultResponse::getData);
        
        StepVerifier.create(data)
        .expectNext(expectedData)
        .verifyComplete();
	}
}
