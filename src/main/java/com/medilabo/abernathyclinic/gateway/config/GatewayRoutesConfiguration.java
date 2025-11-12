package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.medilabo.abernathyclinic.gateway.constants.MicroservicesUriConstants;
import com.medilabo.abernathyclinic.gateway.constants.RegexConstants;

@Configuration
public class GatewayRoutesConfiguration {
	@Bean
	RouteLocator patientRoutes(RouteLocatorBuilder builder) {
	    return builder.routes()
	        .route("get_patients_route", r -> r
	            .path("/patients")
	            .filters(filter -> filter.rewritePath("/patients", "/api/patients"))
	            .uri(MicroservicesUriConstants.MICROSERVICE_PATIENT_URI))
	      
	        // https://cloud.spring.io/spring-cloud-gateway/multi/multi__configuration.html#_configuring_predicates_and_filters_for_discoveryclient_routes
	        .route("get_patient_route", r -> r
	        		// prédicat : ça matche si l'URL entrante suit ce modèle
        		.path("/patient/{uuid}")
        		.filters(filter -> filter
        				// le filtre capture la valeur avec la regex
        				// puis injecte la valeur capturée dans le path du microservice
        				//  avec un named capturing group pour la lisibilité 
        				.rewritePath("/patient/" + RegexConstants.PATIENT_UUID_PATTERN, 
        						"/api/patient/uuid/${uuid}"))
        		.uri(MicroservicesUriConstants.MICROSERVICE_PATIENT_URI))
	        
	        .route("create_patient_route", r -> r 
	        		.path("/patient")
	        		.filters(filter -> filter
	        				.rewritePath("/patient", "/api/patient"))
	        		.uri(MicroservicesUriConstants.MICROSERVICE_PATIENT_URI))
	        
	        .route("update_patient_route", r -> r
	        		.path("/patient/{uuid}/update")
	        		.filters(filter -> filter
	        				.rewritePath("/patient/" + RegexConstants.PATIENT_UUID_PATTERN + "/update", 
	        						"/api/patient/${uuid}/update"))
	        		
	        		.uri(MicroservicesUriConstants.MICROSERVICE_PATIENT_URI))
	        
	        .route("get_patient_report_info", r -> r
	        		.path("/patient/{uuid}/report-info)")
	        		.filters(filter -> filter 
	        				.rewritePath("/patient/" + RegexConstants.PATIENT_UUID_PATTERN + "/report-info", 
	        						"/api/patient/{uuid}/report-info"))
	        		.uri(MicroservicesUriConstants.MICROSERVICE_PATIENT_URI))

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
						.uri(MicroservicesUriConstants.MICROSERVICE_NOTE_URI))
				
				.route("get_note_by_id", r-> r
						.path("/note/{objectId}")
						.filters(filter -> filter
							.rewritePath("/note/" + RegexConstants.OBJECT_ID_PATTERN,  
							"/api/note/${objectId}"))
						.uri(MicroservicesUriConstants.MICROSERVICE_NOTE_URI))
				
				.route("get_note_by_doctor_id", r -> r
						.path("/notes/doctor/{id}")
						.filters(filter -> filter
								.rewritePath("/notes/doctor/" + RegexConstants.ID_PATTERN, 
										"/api/notes/doctor/${id}"))
						.uri(MicroservicesUriConstants.MICROSERVICE_NOTE_URI))
				

				.route("update_note_by_id", r-> r
						.path("/note/{objectId}/update")
						.filters(filter -> filter
								.rewritePath("/note/" + RegexConstants.OBJECT_ID_PATTERN + "/update",  
										"/api/note/${objectId}/update"))
						.uri(MicroservicesUriConstants.MICROSERVICE_NOTE_URI))
				
				.route("create_note", r -> r
						.path("/note/patient/{uuid}")
						.filters(filter -> filter
								.rewritePath("/note/patient/" + RegexConstants.PATIENT_UUID_PATTERN, 
										"/api/note/patient/${uuid}"))
						.uri(MicroservicesUriConstants.MICROSERVICE_NOTE_URI))
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
						.uri(MicroservicesUriConstants.MICROSERVICE_REPORT_URI))
				.build();
	}
}
