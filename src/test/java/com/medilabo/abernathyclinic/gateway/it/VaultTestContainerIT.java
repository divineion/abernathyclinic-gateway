package com.medilabo.abernathyclinic.gateway.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
	
	// désactiver le secret engine
	// argument corresponds to the enabled PATH of the engine, not the TYPE!
	private static final String disableSecretEngine = "secrets disable secret";
	// le réactiver
	private static final String reinitSecretEngine =  "secrets enable -version=1 -path=secret kv";
	// écrire deux secrets
	private static final String writeTestSecret = "kv put " + PATH + CLI_USERNAME + " password=" + CLI_PASSWORD + " username=" + CLI_USERNAME;
	
	
	@Container // déclare le testcontainer - il sera démarré avant que spring ne commence à construire le context
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
	
	// injecter les valeurs des propriétés du container dans le context spring avant le démarrage de l'application
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
		VaultResponse response = vaultSecretReader.readSecret(PATH + CLI_USERNAME);
		
        assertEquals(CLI_PASSWORD, response.getData().get("password"));
        assertEquals(CLI_USERNAME, response.getData().get("username"));
	}
	
	@Test
	public void writeSecretTest() {
        Map<String, Object> testData = Map.of("username", USERNAME, "password", "testpassword");
        VaultResponse response = vaultSecretWriter.writeSecret(PATH + USERNAME, testData);
        
        assertEquals(testData, response.getData());
	}
}
