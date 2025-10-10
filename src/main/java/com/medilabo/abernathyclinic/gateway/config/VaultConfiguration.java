package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

/**
 * This class defines and initializes the {@link VaultTemplate} bean used to interact 
 * with HashiCorp Vault service. 
 * <p>
 * Retrieves Vault connection parameters (host, port, scheme and token) from
 * the application properties and configures a {@link VaultTemplate}. 
 * </p>
 */
@Configuration
public class VaultConfiguration {
	@Value("${vault.host}")
	private String host;
	
	@Value("${vault.port}")
	private int port;
	
	@Value("${vault.scheme}")
	private String scheme;
	
	@Value("${vault.write-token}")
	private String writeToken;
	
	private VaultEndpoint vaultEndpoint;
	
	/**
	 * Creates a {@link VaultTemplate} bean to interact with HashiCorp Vault. 
	 * @return a configured {@link VaultTemplate}
	 */
	@Bean
	VaultTemplate vaultTemplate() {
		vaultEndpoint = VaultEndpoint.create(host, port);
		vaultEndpoint.setScheme(scheme);
		
		return new VaultTemplate(vaultEndpoint, new TokenAuthentication(token));
	}
}
