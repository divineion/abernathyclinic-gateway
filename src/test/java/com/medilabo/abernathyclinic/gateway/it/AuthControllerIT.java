package com.medilabo.abernathyclinic.gateway.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.medilabo.abernathyclinic.gateway.config.TestSecurityConfig;
import com.medilabo.abernathyclinic.gateway.config.TestUserConfig;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


/**
 * Integration tests for the /user endpoint of {@link AuthController}.
 *
 * <p>
 * Verifies authentication via Basic Auth for in-memory test users defined in 
 * {@link TestUserConfig} with security rules from {@link TestSecurityConfig}.
 * Tests include successful access for organizers and doctors, as well as 
 * unauthorized access scenarios (wrong password, unknown user, no credentials).
 * </p>
 */
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestUserConfig.class})
class AuthControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testOrganizerAccess_shouldReturnUserInfo() {
        webTestClient
            .get().uri("/user")
            .headers(headers -> headers.setBasicAuth("organizer1", "organizer1_P@ssw0rd"))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.username").isEqualTo("organizer1")
            .jsonPath("$.roles[0].authority").isEqualTo("ROLE_ORGANIZER");
    }

    @Test
    void testDoctorAccess_shouldReturnUserInfo() {
        webTestClient
            .get().uri("/user")
            .headers(headers -> headers.setBasicAuth("doctor1", "doctor1_P@ssw0rd"))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(4)
            .jsonPath("$.username").isEqualTo("doctor1")
            .jsonPath("$.roles[0].authority").isEqualTo("ROLE_DOCTOR");
    }

    @Test
    void testWithWrongPassword_shoulReturnUnauthorized() {
        webTestClient
            .get().uri("/user")
            .headers(headers -> headers.setBasicAuth("organizer1", "wrong_password"))
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testUnknownUser_shouldReturnUnauthorized() {
        webTestClient
            .get().uri("/user")
            .headers(headers -> headers.setBasicAuth("unknown", "password"))
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testUnauthenticated_shouldReturnUnauthorized() {
        webTestClient
            .get().uri("/user")
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
