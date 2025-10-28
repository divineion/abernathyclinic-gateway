package com.medilabo.abernathyclinic.gateway.vault.unit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.ReactiveVaultTemplate;
import org.springframework.vault.support.VaultResponse;

import com.medilabo.abernathyclinic.gateway.config.VaultSecretWriter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class VaultSecretWriterTest {
	@Mock
	private ReactiveVaultTemplate vaultTemplate;
	
	@Mock
	private ReactiveVaultTemplate vaultReaderTemplate;
	
	@InjectMocks
	private VaultSecretWriter writer;
	
	private static final String PATH = "secret/abernathyclinic-gateway/test/users/";
	private static final String USERNAME = "TestUsername";
	private static final String FULL_PATH = PATH + USERNAME;
	
	// initialisation manuelle pr distinguer les templates
	@BeforeEach
	public void setUp() {
		this.writer = new VaultSecretWriter(vaultTemplate, vaultReaderTemplate);
	}
	
	@Test
	public void writeSecretTest() {
		// Arrange
		VaultResponse expectedResponse = new VaultResponse();
		Map<String, Object> secret = Map.of("username", USERNAME, "password", "testPassword");
		expectedResponse.setData(secret);
				
	    when(vaultTemplate.write(FULL_PATH, secret)).thenReturn(Mono.just(expectedResponse));
		when(vaultReaderTemplate.read(FULL_PATH)).thenReturn(Mono.just(expectedResponse));
		
		// Act and assert
		Mono<Map<String, Object>> writerResult = writer.writeSecret(FULL_PATH, secret)
				.map(VaultResponse::getData);
		
		// StepVerfier s'abonne uu Publisher et le vérifie de manière synchrone
		StepVerifier.create(writerResult)
		// verifies that the data has been correctly extracted
			.expectNext(secret)
		// vérifies that the onComplete event is triggered and 
			.verifyComplete(); // la méthode bloque le test, force le test à attendre que le Mono se termine 
							   // pour vérifier de manière synchrone le résultat du flux
		
		verify(vaultTemplate).write(FULL_PATH, secret);
		verify(vaultReaderTemplate).read(FULL_PATH);
	}
}
