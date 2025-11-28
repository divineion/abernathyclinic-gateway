package com.medilabo.abernathyclinic.gateway.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.ReactiveVaultTemplate;
import org.springframework.vault.support.VaultResponse;

import reactor.core.publisher.Mono;

/**
 * Component responsible for writing secrets into HashiCorp Vault. 
 * <p>
 * 	This component uses the {@link ReactiveVaultTemplate} provided by {@link VaultConfiguration}.
 * 	After writing a secret, it reads the same secret back to verify the operation.
 * 	All operations are non-blocking and return a {@link Mono} wrapping a {@link VaultResponse}.
 * </p>
 */
@Component
public class VaultSecretWriter {
	private final ReactiveVaultTemplate vaultWriterTemplate;
	private final ReactiveVaultTemplate vaultReaderTemplate;
	
	public VaultSecretWriter(@Qualifier("vaultWriterTemplate") ReactiveVaultTemplate vaultWriterTemplate, @Qualifier("vaultReaderTemplate") ReactiveVaultTemplate vaultReaderTemplate) {
		this.vaultWriterTemplate = vaultWriterTemplate;
		this.vaultReaderTemplate = vaultReaderTemplate;
	}
	
	/**
	 * Writes a secret as key-value Map to the provided Vault path.
	 * @param path the Vault storage path
	 * @param secret the map of key-value pair to write. 
	 */
	public Mono<VaultResponse> writeSecret(String path, Map<String, Object> secret) {
		return vaultWriterTemplate.write(path, secret)
				.then(vaultReaderTemplate.read(path));
	}
}
