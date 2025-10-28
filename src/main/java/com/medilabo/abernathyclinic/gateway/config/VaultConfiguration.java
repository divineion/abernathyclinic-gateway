package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.vault.authentication.VaultTokenSupplier;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.ReactiveVaultTemplate;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultToken;

import reactor.core.publisher.Mono;

/**
 * This class defines and initializes {@link VaultTemplate} beans used to interact 
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
	
	@Value("${vault.token.users.write}")
	private String writeToken;
	
	@Value("${vault.token.users.read}")
	private String readToken;
		
	private VaultEndpoint getVaultEndpoint() {
		VaultEndpoint endpoint = VaultEndpoint.create(host, port);
		endpoint.setScheme(scheme);
		
		return endpoint;
	}

	
	// send requests via Reactor Netty
	// default configuration
	@Bean
    ClientHttpConnector clientHttpConnector() {
        return new ReactorClientHttpConnector();
    }

	/**
	 * Creates a {@link ReactiveVaultTemplate} bean to write into HashiCorp Vault. 
	 * @return a configured {@link ReactiveVaultTemplate}
	 */
	@Bean("vaultWriterTemplate")
	ReactiveVaultTemplate vaultWriterTemplate(ClientHttpConnector connector) {
				
		VaultTokenSupplier writeTokenSupplier = () -> Mono.just(VaultToken.of(writeToken));

		return new ReactiveVaultTemplate(getVaultEndpoint(), connector, writeTokenSupplier);
	}
	
	/**
	 * Creates a {@link ReactiveVaultTemplate} bean to read secrets from HashiCorp Vault. 
	 * @return a configured {@link ReactiveVaultTemplate}
	 */
	@Bean("vaultReaderTemplate")
	ReactiveVaultTemplate vaultReaderTemplate(ClientHttpConnector connector) {
		VaultTokenSupplier readerTokenSupplier = () -> Mono.just(VaultToken.of(readToken));
		
		return new ReactiveVaultTemplate(getVaultEndpoint(), connector, readerTokenSupplier);
	}
}
