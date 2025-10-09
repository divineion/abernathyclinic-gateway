package com.medilabo.abernathyclinic.gateway.config;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;

/**
 * Component responsible for writing secrets into HashiCorp Vault. 
 * <p>
 * This component uses the {@link VaultTemplate} provided by {@link VaultConfiguration}.
 * </p>
 */
@Component
public class VaultSecretWriter {
	private final VaultTemplate vaultTemplate;
	
	public VaultSecretWriter(VaultTemplate vaultTemplate) {
		this.vaultTemplate = vaultTemplate;
	}
	
	/**
	 * Writes a secret as key-value Map to the provided Vault path.
	 * @param path the Vault storage path
	 * @param secret the map of key-value pair to write. 
	 */
	public void writeSecret(String path, Map<String, String> secret) {
		vaultTemplate.write(path, secret);
	}
}
