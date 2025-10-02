package com.medilabo.abernathyclinic.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.medilabo.abernathyclinic.gateway.MicroservicesUriConstants;

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
        		.path("/patient/{id}")
        		.filters(filter -> filter
        				// le filtre capture la valeur avec la regex
        				// puis injecte la valeur capturée dans le path du microservice
        				//  avec un named capturing group pour la lisibilité 
        				.rewritePath("/patient/(?<id>.*)", "/api/patient/${id}"))
        		.uri(MicroservicesUriConstants.MICROSERVICE_PATIENT_URI))
	        .build();
	}
}
