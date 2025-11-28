package com.medilabo.abernathyclinic.gateway.config;

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestUserConfig {

    @Bean
    CustomMapReactiveUserDetailsService testUserDetailsService(PasswordEncoder encoder) {

        AppUser organizer1 = new AppUser(
            1,
            null,
            "organizer1",
            encoder.encode("organizer1_P@ssw0rd"),
            List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")),
            null,
            null
        );

        AppUser organizer2 = new AppUser(
            2,
            null,
            "organizer2",
            encoder.encode("organizer2_P@ssw0rd"),
            List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")),
            null,
            null
        );

        AppUser organizer3 = new AppUser(
            3,
            null,
            "organizer3",
            encoder.encode("organizer3_P@ssw0rd"),
            List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER")),
            null,
            null
        );

        AppUser doctor1 = new AppUser(
            4,
            null,
            "doctor1",
            encoder.encode("doctor1_P@ssw0rd"),
            List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")),
            null,
            null
        );

        AppUser doctor2 = new AppUser(
            5,
            null,
            "doctor2",
            encoder.encode("doctor2_P@ssw0rd"),
            List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")),
            null,
            null
        );

        AppUser doctor3 = new AppUser(
            6,
            null,
            "doctor3",
            encoder.encode("doctor3_P@ssw0rd"),
            List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")),
	            null,
	            null
	        );

	        return new CustomMapReactiveUserDetailsService(
	            List.of(
	                organizer1, organizer2, organizer3,
	                doctor1, doctor2, doctor3
	            )
	        );
    }
}

