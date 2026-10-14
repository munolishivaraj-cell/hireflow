package com.shivaraj.hireflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

	@Bean
	OpenAPI hireFlowOpenApi() {
		return new OpenAPI().info(new Info().title("HireFlow API")
			.version("0.0.1")
			.description("""
					Recruitment / applicant tracking demo built with Spring Boot, MySQL, Kafka and Redis.

					Try it: create a job and a candidate, apply, then fetch the application a few seconds \
					later to see the match score computed by the Kafka screening consumer. Candidate \
					notifications are written by a second consumer group. Job details and pipeline counts \
					are cached in Redis, and applying is rate limited per candidate.

					This is a public demo: please don't enter real personal data.
					"""));
	}
}
