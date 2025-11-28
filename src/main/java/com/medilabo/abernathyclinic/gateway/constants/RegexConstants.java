package com.medilabo.abernathyclinic.gateway.constants;

import java.util.UUID;

import com.medilabo.abernathyclinic.gateway.config.GatewayRoutesConfiguration;

/**
 * Define regex and named capturing groups for matching specific formats and capturing corresponding characters.
 * Theses patterns are used in {@link GatewayRoutesConfiguration} to rewrite request paths.  
 */
public class RegexConstants {
	private RegexConstants(){}
	
	/**
	 * Regex pattern for matching a valid {@link UUID} format. 
	 * <p>Defines the named capturing group: "uuid".</p>
	 */
	public static final String PATIENT_UUID_PATTERN = "(?<uuid>[a-fA-F0-9\\-]{36})";
	
	/**
	 * Regex pattern for matching a valid MongoDB ObjectId format.
	 * <p>Defines the named capturing group: "objectId".</p>
	 */
	public static final String OBJECT_ID_PATTERN = "(?<objectId>[0-9a-fA-F]{24})";

	public static final String ID_PATTERN = "(?<id>\\d+)";
}