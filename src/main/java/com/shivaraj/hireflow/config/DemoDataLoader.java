package com.shivaraj.hireflow.config;

import java.util.List;
import java.util.Set;

import com.shivaraj.hireflow.application.ApplicationResponse;
import com.shivaraj.hireflow.application.ApplicationService;
import com.shivaraj.hireflow.application.ApplyRequest;
import com.shivaraj.hireflow.application.Stage;
import com.shivaraj.hireflow.application.StageChangeRequest;
import com.shivaraj.hireflow.candidate.CandidateRequest;
import com.shivaraj.hireflow.candidate.CandidateService;
import com.shivaraj.hireflow.job.JobRepository;
import com.shivaraj.hireflow.job.JobRequest;
import com.shivaraj.hireflow.job.JobService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds a few fictional jobs, candidates and applications so the public demo has something to explore.
 * Runs only with the "demo" profile and only when the database has no jobs yet.
 */
@Component
@Profile("demo")
public class DemoDataLoader {

	private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

	private final JobRepository jobRepository;

	private final JobService jobService;

	private final CandidateService candidateService;

	private final ApplicationService applicationService;

	public DemoDataLoader(JobRepository jobRepository, JobService jobService, CandidateService candidateService,
			ApplicationService applicationService) {
		this.jobRepository = jobRepository;
		this.jobService = jobService;
		this.candidateService = candidateService;
		this.applicationService = applicationService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void seed() {
		if (jobRepository.count() > 0) {
			return;
		}
		long backend = jobService.create(new JobRequest("Java Backend Engineer", "Engineering", "Bengaluru", 3,
				"Build Spring Boot microservices backed by MySQL, Kafka and Redis.",
				Set.of("java", "spring boot", "kafka", "redis", "mysql")))
			.id();
		long fullStack = jobService.create(new JobRequest("Full Stack Developer", "Product", "Remote", 2,
				"Own features end to end across a Spring Boot API and a React/TypeScript UI.",
				Set.of("java", "spring boot", "react", "typescript")))
			.id();
		long devops = jobService.create(new JobRequest("Platform Engineer", "Infrastructure", "Hyderabad", 4,
				"Run Docker-based services, CI/CD pipelines and observability.",
				Set.of("docker", "kubernetes", "aws", "ci/cd")))
			.id();

		long asha = candidateService.register(new CandidateRequest("Asha Rao (demo)", "asha.demo@example.com", null,
				4, Set.of("java", "spring boot", "kafka", "mysql")))
			.id();
		long vikram = candidateService.register(new CandidateRequest("Vikram Shetty (demo)",
				"vikram.demo@example.com", null, 2, Set.of("java", "react", "typescript")))
			.id();
		long meera = candidateService.register(new CandidateRequest("Meera Iyer (demo)", "meera.demo@example.com",
				null, 6, Set.of("docker", "aws", "ci/cd", "java")))
			.id();

		ApplicationResponse a1 = apply(backend, asha);
		advance(a1.id(), List.of(Stage.SCREENING, Stage.INTERVIEW));
		ApplicationResponse a2 = apply(backend, vikram);
		advance(a2.id(), List.of(Stage.REJECTED));
		ApplicationResponse a3 = apply(fullStack, vikram);
		advance(a3.id(), List.of(Stage.SCREENING));
		apply(devops, meera);
		apply(fullStack, asha);
		log.info("Seeded demo data: 3 jobs, 3 candidates, 5 applications");
	}

	private ApplicationResponse apply(long jobId, long candidateId) {
		return applicationService.apply(new ApplyRequest(jobId, candidateId, "Seeded demo application"));
	}

	private void advance(long applicationId, List<Stage> stages) {
		for (Stage stage : stages) {
			applicationService.changeStage(applicationId, new StageChangeRequest(stage, "Demo data"));
		}
	}
}
