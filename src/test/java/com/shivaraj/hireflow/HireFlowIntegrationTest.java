package com.shivaraj.hireflow;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;

import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full flow against real MySQL, Redis and Kafka containers: post a job, register a candidate, apply, and check
 * that the Kafka consumers scored the application and notified the candidate.
 */
@SpringBootTest(properties = "hireflow.apply-limit.max=2")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class HireFlowIntegrationTest {

	@Container
	@ServiceConnection
	static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

	@Container
	@ServiceConnection
	static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.9.1");

	@Container
	@ServiceConnection(name = "redis")
	static GenericContainer<?> redis = new GenericContainer<>("redis:7.4").withExposedPorts(6379);

	@Autowired
	MockMvc mvc;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Test
	void applicationFlowsThroughKafkaToScoringAndNotifications() throws Exception {
		long jobId = createJob("""
				{"title": "Java Backend Engineer", "department": "Engineering", "location": "Bengaluru",
				 "minExperienceYears": 3, "description": "Spring Boot microservices",
				 "skills": ["Java", "Spring Boot", "Kafka", "Redis"]}
				""");
		long candidateId = idOf(mvc.perform(post("/api/candidates").contentType(MediaType.APPLICATION_JSON).content("""
				{"fullName": "Shivaraj Munoli", "email": "candidate@example.com", "experienceYears": 3,
				 "skills": ["java", "spring boot", "mysql"]}
				""")).andExpect(status().isCreated()).andReturn());

		long applicationId = idOf(mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
			.content("{\"jobId\": %d, \"candidateId\": %d}".formatted(jobId, candidateId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.stage").value("APPLIED"))
			.andReturn());

		// Screening consumer: 2 of 4 skills (35) + experience met (30)
		await().atMost(Duration.ofSeconds(30))
			.untilAsserted(() -> mvc.perform(get("/api/applications/{id}", applicationId))
				.andExpect(jsonPath("$.matchScore").value(65)));

		// Duplicate application is blocked by the unique constraint check
		mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
			.content("{\"jobId\": %d, \"candidateId\": %d}".formatted(jobId, candidateId)))
			.andExpect(status().isConflict());

		mvc.perform(patch("/api/applications/{id}/stage", applicationId).contentType(MediaType.APPLICATION_JSON)
			.content("{\"stage\": \"SCREENING\", \"note\": \"Strong Spring background\"}"))
			.andExpect(status().isOk());
		mvc.perform(patch("/api/applications/{id}/stage", applicationId).contentType(MediaType.APPLICATION_JSON)
			.content("{\"stage\": \"HIRED\"}"))
			.andExpect(status().isUnprocessableContent());

		// Notification consumer: one message for the submission, one for the stage change
		await().atMost(Duration.ofSeconds(30))
			.untilAsserted(() -> mvc.perform(get("/api/candidates/{id}/notifications", candidateId))
				.andExpect(jsonPath("$", hasSize(2))));

		mvc.perform(get("/api/applications/{id}/history", applicationId)).andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get("/api/jobs/{id}/pipeline", jobId))
			.andExpect(jsonPath("$.total").value(1))
			.andExpect(jsonPath("$.byStage.SCREENING").value(1));

		// Job details and pipeline are now cached in Redis
		mvc.perform(get("/api/jobs/{id}", jobId)).andExpect(status().isOk());
		assertThat(redisTemplate.hasKey("hireflow:jobs::" + jobId)).isTrue();
		assertThat(redisTemplate.hasKey("hireflow:pipeline::" + jobId)).isTrue();

		mvc.perform(get("/api/jobs").param("skill", "kafka")).andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void applyIsRateLimitedPerCandidateInRedis() throws Exception {
		long candidateId = idOf(mvc.perform(post("/api/candidates").contentType(MediaType.APPLICATION_JSON).content("""
				{"fullName": "Busy Applicant", "email": "busy@example.com", "experienceYears": 1,
				 "skills": ["java"]}
				""")).andReturn());
		String job = """
				{"title": "Engineer", "department": "Eng", "location": "Pune", "minExperienceYears": 0,
				 "skills": ["java"]}
				""";
		for (int i = 0; i < 2; i++) {
			mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
				.content("{\"jobId\": %d, \"candidateId\": %d}".formatted(createJob(job), candidateId)))
				.andExpect(status().isCreated());
		}
		mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
			.content("{\"jobId\": %d, \"candidateId\": %d}".formatted(createJob(job), candidateId)))
			.andExpect(status().isTooManyRequests());
	}

	private long createJob(String json) throws Exception {
		return idOf(mvc.perform(post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().isCreated())
			.andReturn());
	}

	private static long idOf(MvcResult result) throws Exception {
		return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
	}
}
