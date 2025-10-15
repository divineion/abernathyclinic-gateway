package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

/**
 * Component responsible for reading secrets from HashiCorp Vault. 
 * <p>
 * This component uses the {@link VaultTemplate} provided by {@link VaultConfiguration}.
 * </p>
 */
@Component
public class VaultSecretReader {
	private final VaultTemplate vaultTemplate;
	
	public VaultSecretReader(@Qualifier("vaultReaderTemplate") VaultTemplate vaultTemplate) {
		this.vaultTemplate = vaultTemplate;
	}
	
	/**
	 * Reads a secret from the provided Vault path.
	 * @param path the Vault storage path
	 */
	public VaultResponse readSecret(String path) {
		return vaultTemplate.read(path);
	}
}
