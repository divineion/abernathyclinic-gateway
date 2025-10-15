package com.medilabo.abernathyclinic.gateway.vault.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import com.medilabo.abernathyclinic.gateway.config.VaultSecretWriter;

@ExtendWith(MockitoExtension.class)
public class VaultSecretWriterTest {
	@Mock
	private VaultTemplate vaultTemplate;
	
	@Mock
	private VaultTemplate vaultReaderTemplate;
	
	@InjectMocks
	private VaultSecretWriter writer;
	
	private static final String PATH = "secret/abernathyclinic-gateway/dev/users/";
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
				
		when(vaultReaderTemplate.read(FULL_PATH)).thenReturn(expectedResponse);
		
		// Act
		VaultResponse response = writer.writeSecret(FULL_PATH, secret);
		
		// Assert
		assertEquals(secret, response.getData());
		
		verify(vaultTemplate).write(FULL_PATH, secret);
		verify(vaultReaderTemplate).read(FULL_PATH);
	}
}
