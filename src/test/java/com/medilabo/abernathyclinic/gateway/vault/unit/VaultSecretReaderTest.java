package com.medilabo.abernathyclinic.gateway.vault.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import com.medilabo.abernathyclinic.gateway.config.VaultSecretReader;

@ExtendWith(MockitoExtension.class)
public class VaultSecretReaderTest {
	@Mock
	private VaultTemplate mockVaultTemplate;
	
	@InjectMocks
	private VaultSecretReader reader;
	
	private static final String TEST_PATH = "secret/abernathyclinic-gateway/dev/users/organizer1";
	
	@Test
	public void testReadSecret_shouldReturnVaultResponse() {
		// Arrange
		Map<String, Object> expectedData = Map.of("password", "testPassword");
		VaultResponse mockVaultResponse = new VaultResponse();
		mockVaultResponse.setData(expectedData);
		
		when(mockVaultTemplate.read(TEST_PATH)).thenReturn(mockVaultResponse);
		
		// Act
		VaultResponse response = reader.readSecret(TEST_PATH);
		
		// Assert		
		assertEquals(expectedData, response.getData());
		
		verify(mockVaultTemplate).read(TEST_PATH);
	}
}
