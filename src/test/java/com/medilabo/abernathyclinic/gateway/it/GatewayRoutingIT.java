package com.medilabo.abernathyclinic.gateway.it;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.medilabo.abernathyclinic.gateway.config.TestRoutingSecurityConfig;

/**
 * Integration tests for the API Gateway routing.
 * Verifies that public URLs are correctly routed to micro-services.
 * Verifies path rewriting, headers, and body forwarding work correctly.
 * Uses WireMock servers to simulate Patient, Notes, and Report micro-services.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestRoutingSecurityConfig.class)
public class GatewayRoutingIT {
	@LocalServerPort
    int port;
	
	WebTestClient webTestClient;
	
	WireMockServer patientMock;
	WireMockServer notesMock;
	WireMockServer reportMock;
		
	@BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToServer()
                                     .baseUrl("http://localhost:" + port)
                                     .build();

        patientMock = new WireMockServer(8097);
        notesMock = new WireMockServer(8098);
        reportMock = new WireMockServer(8099);

        patientMock.start();
        notesMock.start();
        reportMock.start();
    }
	
	@AfterEach
    void teardown() {
        patientMock.stop();
        notesMock.stop();
        reportMock.stop();
    }
	
	@Test
	void testGetPatientRoute() {
	    String uuid = "123e4567-e89b-12d3-a456-426614174000";

	    patientMock.stubFor(get("/api/patient/" + uuid)
	            .willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", "application/json")
	                .withBody("{\"id\":\"" + uuid + "\",\"name\":\"John Doe\"}")));

	    webTestClient.get()
	        .uri("/patient/{uuid}", uuid) 
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody()
	        .jsonPath("$.id").isEqualTo(uuid)
	        .jsonPath("$.name").isEqualTo("John Doe");
	}

	@Test
	void testGetPatientsRoute() {
	    patientMock.stubFor(get("/api/patients")
	        .willReturn(aResponse()
	            .withStatus(200)
	            .withHeader("Content-Type", "application/json")
	            .withBody("[{\"uuid\":\"123\",\"name\":\"John\"}]")));

	    webTestClient.get().uri("/patients")
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody().jsonPath("$[0].uuid").isEqualTo("123");
	}

	@Test
	void testCreatePatientRoute() {
	    String patientJson = "{\"name\":\"Jane Doe\"}";
	    
	    patientMock.stubFor(post("/api/patient")
	        .willReturn(aResponse()
	            .withStatus(201)
	            .withHeader("Content-Type", "application/json")
	            .withBody("{\"uuid\":\"456\",\"name\":\"Jane Doe\"}")));

	    webTestClient.post().uri("/patient")
	        .contentType(MediaType.APPLICATION_JSON)
	        .bodyValue(patientJson)
	        .exchange()
	        .expectStatus().isCreated()
	        .expectBody().jsonPath("$.name").isEqualTo("Jane Doe");
	}

	@Test
	void testUpdatePatientRoute() {
	    String uuid = "123e4567-e89b-12d3-a456-426614174000";
	    
	    patientMock.stubFor(put("/api/patient/" + uuid + "/update")
	        .willReturn(aResponse().withStatus(204)));

	    webTestClient.put().uri("/patient/{uuid}/update", uuid)
	        .exchange()
	        .expectStatus().isNoContent();
	}

	@Test
	void testGetPatientReportInfo() {
	    String uuid = "123e4567-e89b-12d3-a456-426614174000";
	    
	    patientMock.stubFor(get("/api/patient/" + uuid + "/report-info")
	        .willReturn(aResponse()
	            .withStatus(200)
	            .withBody("{\"reportAvailable\":true}")));

	    webTestClient.get().uri("/patient/{uuid}/report-info", uuid)
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody().jsonPath("$.reportAvailable").isEqualTo(true);
	}

	// NOTES ROUTES
	@Test
	void testGetNotesByPatient() {
	    String uuid = "123e4567-e89b-12d3-a456-426614174000";
	    
	    notesMock.stubFor(get("/api/notes/patient/" + uuid)
	        .willReturn(aResponse()
	            .withStatus(200)
	            .withBody("[{\"id\":\"note1\",\"content\":\"Note1\"}]")));

	    webTestClient.get().uri("/notes/patient/{uuid}", uuid)
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody().jsonPath("$[0].id").isEqualTo("note1");
	}

	@Test
	void testGetNoteById() {
	    String objectId = "507f1f77bcf86cd799439011";
	    
	    notesMock.stubFor(get("/api/note/" + objectId)
	        .willReturn(aResponse()
	            .withStatus(200)
	            .withBody("{\"objectId\":\"" + objectId + "\",\"content\":\"Note\"}")));

	    webTestClient.get().uri("/note/{objectId}", objectId)
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody().jsonPath("$.objectId").isEqualTo(objectId);
	}

	@Test
	void testGetNotesByDoctor() {
	    String doctorId = "123";
	    
	    notesMock.stubFor(get("/api/notes/doctor/" + doctorId)
	        .willReturn(aResponse().withStatus(200).withBody("[]")));

	    webTestClient.get().uri("/notes/doctor/{id}", doctorId)
	        .exchange()
	        .expectStatus().isOk();
	}

	@Test
	void testUpdateNoteById() {
	    String objectId = "507f1f77bcf86cd799439011";
	    
	    notesMock.stubFor(put("/api/note/" + objectId + "/update")
	        .willReturn(aResponse().withStatus(204)));

	    webTestClient.put().uri("/note/{objectId}/update", objectId)
	        .exchange()
	        .expectStatus().isNoContent();
	}

	@Test
	void testCreateNote() {
	    String uuid = "123e4567-e89b-12d3-a456-426614174000";
	    String noteJson = "{\"content\":\"New note\"}";
	    
	    notesMock.stubFor(post("/api/note/patient/" + uuid)
	        .willReturn(aResponse().withStatus(201)));

	    webTestClient.post().uri("/note/patient/{uuid}", uuid)
	        .contentType(MediaType.APPLICATION_JSON)
	        .bodyValue(noteJson)
	        .exchange()
	        .expectStatus().isCreated();
	}

	@Test
	void testGetNotesReportInfo() {
	    String uuid = "123e4567-e89b-12d3-a456-426614174000";
	    
	    notesMock.stubFor(get("/api/notes/" + uuid + "/report-info")
	        .willReturn(aResponse().withStatus(200).withBody("{}")));

	    webTestClient.get().uri("/notes/{uuid}/report-info", uuid)
	        .exchange()
	        .expectStatus().isOk();
	}

	// REPORT ROUTE
	@Test
	void testGetPatientReport() {
	    String uuid = "123e4567-e89b-12d3-a456-426614174000";
	    
	    reportMock.stubFor(get("/api/report/" + uuid)
	        .willReturn(aResponse()
	            .withStatus(200)
	            .withHeader("Content-Type", "application/pdf")
	            .withBody(new byte[100])));

	    webTestClient.get().uri("/report/{uuid}", uuid)
	        .exchange()
	        .expectStatus().isOk()
	        .expectHeader().contentType("application/pdf");
	}
}

