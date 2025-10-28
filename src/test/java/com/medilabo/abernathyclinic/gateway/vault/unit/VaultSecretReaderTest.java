package com.medilabo.abernathyclinic.gateway.vault.unit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.vault.core.ReactiveVaultTemplate;
import org.springframework.vault.support.VaultResponse;

import com.medilabo.abernathyclinic.gateway.config.VaultSecretReader;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class VaultSecretReaderTest {
	@Mock
	private ReactiveVaultTemplate mockVaultTemplate;
	
	@InjectMocks
	private VaultSecretReader reader;
	
	private static final String TEST_PATH = "secret/abernathyclinic-gateway/test/users/organizer1";
	
	/**
	 * VaultSecretReader a 2 rôles : initialiser un VaultTemplate, et retourner un {@link Mono}<{@link VaultResponse}>
	 * 
	 */
	@Test
	public void testReadSecret_shouldReturnVaultResponse() {
		// Arrange
		Map<String, Object> expectedData = Map.of("password", "testPassword");
		VaultResponse mockVaultResponse = new VaultResponse();
		mockVaultResponse.setData(expectedData);
		
		when(mockVaultTemplate.read(TEST_PATH)).thenReturn(Mono.just(mockVaultResponse));
		
		// Act
		Mono<Map<String, Object>> fetchedData = reader.readSecret(TEST_PATH)
				.map(VaultResponse::getData); // transformation : le Mono contient désormais le Map<String, Object> issue du VaultResponse
			
		// https://projectreactor.io/docs/core/release/reference/testing.html
		// StepVerifier va wrapper le Mono et le vérifier en se comportant comme un abonné
		StepVerifier.create(fetchedData)
			.expectNext(expectedData) // vérifier si le signal onNext déclenche l'émissions des bonnes données
			.verifyComplete();

		verify(mockVaultTemplate).read(TEST_PATH);
	}
}
