package com.medilabo.abernathyclinic.gateway.config;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class AuthController {
	// permpet de retourner les rôles au front
	@GetMapping("/user")
	public Map<String, Object> user(Authentication authentication) {
		if (authentication == null) {
			return Map.of("authenticated", false);
		} 
		
		Object principal = authentication.getPrincipal();
		
		// récupérer le user authentifié dans une variable pour récup les données  
		if (principal instanceof AppUser appUser) {
			return Map.of(
				"id", appUser.getId(),
				"username", appUser.getUsername(),
				"roles", appUser.getAuthorities());
		}
		
		// solution de repli si le principal n'est pas AppUser	
		return Map.of(
	            "username", authentication.getName(),
	            "roles", authentication.getAuthorities()
	        );
		

	}
}
