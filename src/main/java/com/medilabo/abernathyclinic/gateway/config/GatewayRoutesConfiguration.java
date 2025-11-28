package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.medilabo.abernathyclinic.gateway.constants.RegexConstants;

/**
 * Configures routes for Spring Cloud Gateway.
 * 
 * <p>This class defines how incoming HTTP requests to the gateway are routed to the
 * appropriate micro-services: Patient, Notes, and Report.</p>
 * 
 * <p>
 * <ul>
 *   <li>routes requests to backend micro-services based on path patterns</li>
 *   <li>rewrites paths to match the endpoints expected by the target micro-service</li>
 *   <li>supports route parameters using regex constants for UUIDs and object IDs</li>
 * </ul>
 * </p>
 * 
 * <p>Provides three RouteLocator beans:
 * <ul>
 *   <li>{@link #patientRoutes(RouteLocatorBuilder)} - routes for patient service</li>
 *   <li>{@link #notesRoutes(RouteLocatorBuilder)} - routes for notes service</li>
 *   <li>{@link #reportRoute(RouteLocatorBuilder)} - routes for report service</li>
 * </ul>
 * </p>
 * 
 */
@Configuration
public class GatewayRoutesConfiguration {
	@Value("${patient.service.url}")
	private String patientServiceUri;
	 
	 @Value("${notes.service.url}")
	private String notesServiceUri;
	 
	 @Value("${report.service.url}")
	private String reportServiceUri;

	@Bean
	RouteLocator patientRoutes(RouteLocatorBuilder builder) {
	    return builder.routes()
	        .route("get_patients_route", r -> r
	            .path("/patients")
	            .filters(filter -> filter.rewritePath("/patients", "/api/patients"))
	            .uri(patientServiceUri))
	      
	        .route("get_patient_route", r -> r
        		.path("/patient/{uuid}")
        		.filters(filter -> filter
        				.rewritePath("/patient/" + RegexConstants.PATIENT_UUID_PATTERN, 
        						"/api/patient/uuid/${uuid}"))
        		.uri(patientServiceUri))
	        
	        .route("create_patient_route", r -> r 
	        		.path("/patient")
	        		.filters(filter -> filter
	        				.rewritePath("/patient", "/api/patient"))
	        		.uri(patientServiceUri))
	        
	        .route("update_patient_route", r -> r
	        		.path("/patient/{uuid}/update")
	        		.filters(filter -> filter
	        				.rewritePath("/patient/" + RegexConstants.PATIENT_UUID_PATTERN + "/update", 
	        						"/api/patient/${uuid}/update"))
	        		
	        		.uri(patientServiceUri))
	        
	        .route("get_patient_report_info", r -> r
	        		.path("/patient/{uuid}/report-info")
	        		.filters(filter -> filter 
	        				.rewritePath("/patient/" + RegexConstants.PATIENT_UUID_PATTERN + "/report-info", 
	        						"/api/patient/${uuid}/report-info"))
	        		.uri(patientServiceUri))

	        .build();
	}
	
	@Bean
	RouteLocator notesRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("get_notes_by_patient_route", r -> r
						.path("/notes/patient/{uuid}")
						.filters(filter -> filter
								.rewritePath("/notes/patient/" + RegexConstants.PATIENT_UUID_PATTERN, 
										"/api/notes/patient/${uuid}"))
						.uri(notesServiceUri))
				
				.route("get_note_by_id", r-> r
						.path("/note/{objectId}")
						.filters(filter -> filter
							.rewritePath("/note/" + RegexConstants.OBJECT_ID_PATTERN,  
							"/api/note/${objectId}"))
						.uri(notesServiceUri))
				
				.route("get_note_by_doctor_id", r -> r
						.path("/notes/doctor/{id}")
						.filters(filter -> filter
								.rewritePath("/notes/doctor/" + RegexConstants.ID_PATTERN, 
										"/api/notes/doctor/${id}"))
						.uri(notesServiceUri))
				

				.route("update_note_by_id", r-> r
						.path("/note/{objectId}/update")
						.filters(filter -> filter
								.rewritePath("/note/" + RegexConstants.OBJECT_ID_PATTERN + "/update",  
										"/api/note/${objectId}/update"))
						.uri(notesServiceUri))
				
				.route("create_note", r -> r
						.path("/note/patient/{uuid}")
						.filters(filter -> filter
								.rewritePath("/note/patient/" + RegexConstants.PATIENT_UUID_PATTERN, 
										"/api/note/patient/${uuid}"))
						.uri(notesServiceUri))
				
				.route("get_notes_report_info", r -> r
		        		.path("/notes/{uuid}/report-info")
		        		.filters(filter -> filter 
		        				.rewritePath("/notes/" + RegexConstants.PATIENT_UUID_PATTERN + "/report-info", 
		        						"/api/notes/${uuid}/report-info"))
		        		.uri(notesServiceUri))		
				.build();
	}
	
	@Bean
	RouteLocator reportRoute(RouteLocatorBuilder builder) {
		return builder.routes()	
				.route("get_patient_report", r -> r
						.path("/report/{uuid}")
						.filters(filter -> filter
								.rewritePath("/report/" + RegexConstants.PATIENT_UUID_PATTERN, 
										"/api/report/${uuid}"))
						.uri(reportServiceUri))
				.build();
	}
}
