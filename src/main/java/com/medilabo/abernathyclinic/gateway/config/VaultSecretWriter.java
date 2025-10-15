package com.medilabo.abernathyclinic.gateway.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

/**
 * Component responsible for writing secrets into HashiCorp Vault. 
 * <p>
 * This component uses the {@link VaultTemplate} provided by {@link VaultConfiguration}.
 * </p>
 */
@Component
public class VaultSecretWriter {
	private final VaultTemplate vaultWriterTemplate;
	private final VaultTemplate vaultReaderTemplate;
	
	public VaultSecretWriter(@Qualifier("vaultWriterTemplate") VaultTemplate vaultWriterTemplate, @Qualifier("vaultReaderTemplate") VaultTemplate vaultReaderTemplate) {
		this.vaultWriterTemplate = vaultWriterTemplate;
		this.vaultReaderTemplate = vaultReaderTemplate;
	}
	
	/**
	 * Writes a secret as key-value Map to the provided Vault path.
	 * @param path the Vault storage path
	 * @param secret the map of key-value pair to write. 
	 */
	public VaultResponse writeSecret(String path, Map<String, Object> secret) {
		vaultWriterTemplate.write(path, secret);
		
		return vaultReaderTemplate.read(path);
	}
}
