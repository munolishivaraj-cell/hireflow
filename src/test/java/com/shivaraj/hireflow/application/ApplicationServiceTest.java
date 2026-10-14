package com.shivaraj.hireflow.application;

import java.util.Optional;
import java.util.Set;

import com.shivaraj.hireflow.candidate.Candidate;
import com.shivaraj.hireflow.candidate.CandidateService;
import com.shivaraj.hireflow.common.ApiException;
import com.shivaraj.hireflow.config.CacheNames;
import com.shivaraj.hireflow.event.HiringEvent;
import com.shivaraj.hireflow.job.Job;
import com.shivaraj.hireflow.job.JobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

	@Mock
	ApplicationRepository applications;

	@Mock
	StageHistoryRepository history;

	@Mock
	JobService jobService;

	@Mock
	CandidateService candidateService;

	@Mock
	ApplyRateLimiter rateLimiter;

	@Mock
	ApplicationEventPublisher events;

	@Mock
	CacheManager cacheManager;

	@Mock
	Cache pipelineCache;

	ApplicationService service;

	Job job;

	Candidate candidate;

	@BeforeEach
	void setUp() {
		service = new ApplicationService(applications, history, jobService, candidateService, rateLimiter,
				new MatchScoreCalculator(), events, cacheManager);
		job = new Job("Backend Engineer", "Engineering", "Bengaluru", 3, "Spring Boot services",
				Set.of("java", "spring boot", "kafka"));
		ReflectionTestUtils.setField(job, "id", 10L);
		candidate = new Candidate("Asha Rao", "asha@example.com", null, 4, Set.of("java", "spring boot"));
		ReflectionTestUtils.setField(candidate, "id", 20L);
	}

	@Test
	void applySavesApplicationAndPublishesSubmittedEvent() {
		given(jobService.load(10L)).willReturn(job);
		given(candidateService.load(20L)).willReturn(candidate);
		given(applications.save(any())).willAnswer(inv -> withId(inv.getArgument(0), 30L));
		given(cacheManager.getCache(CacheNames.PIPELINE)).willReturn(pipelineCache);

		ApplicationResponse response = service.apply(new ApplyRequest(10L, 20L, "Keen to join"));

		assertThat(response.id()).isEqualTo(30L);
		assertThat(response.stage()).isEqualTo(Stage.APPLIED);
		ArgumentCaptor<HiringEvent> event = ArgumentCaptor.forClass(HiringEvent.class);
		then(events).should().publishEvent(event.capture());
		assertThat(event.getValue().type()).isEqualTo(HiringEvent.Type.APPLICATION_SUBMITTED);
		assertThat(event.getValue().candidateEmail()).isEqualTo("asha@example.com");
		then(pipelineCache).should().evict(10L);
	}

	@Test
	void applyToClosedJobIsRejected() {
		job.close();
		given(jobService.load(10L)).willReturn(job);

		assertThatThrownBy(() -> service.apply(new ApplyRequest(10L, 20L, null))).isInstanceOf(ApiException.class)
			.extracting("status")
			.isEqualTo(HttpStatus.CONFLICT);
		then(applications).should(never()).save(any());
	}

	@Test
	void duplicateApplicationIsRejected() {
		given(jobService.load(10L)).willReturn(job);
		given(candidateService.load(20L)).willReturn(candidate);
		given(applications.existsByJobIdAndCandidateId(10L, 20L)).willReturn(true);

		assertThatThrownBy(() -> service.apply(new ApplyRequest(10L, 20L, null))).isInstanceOf(ApiException.class)
			.hasMessageContaining("already applied");
	}

	@Test
	void invalidStageJumpIsRejectedWithoutPublishing() {
		JobApplication application = withId(new JobApplication(job, candidate, null), 30L);
		given(applications.findDetailedById(30L)).willReturn(Optional.of(application));

		assertThatThrownBy(() -> service.changeStage(30L, new StageChangeRequest(Stage.HIRED, null)))
			.isInstanceOf(ApiException.class)
			.extracting("status")
			.isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
		then(events).should(never()).publishEvent(any());
	}

	@Test
	void validStageChangeRecordsHistoryAndPublishesEvent() {
		JobApplication application = withId(new JobApplication(job, candidate, null), 30L);
		given(applications.findDetailedById(30L)).willReturn(Optional.of(application));
		given(cacheManager.getCache(CacheNames.PIPELINE)).willReturn(pipelineCache);

		ApplicationResponse response = service.changeStage(30L, new StageChangeRequest(Stage.SCREENING, "Looks good"));

		assertThat(response.stage()).isEqualTo(Stage.SCREENING);
		then(history).should().save(any(StageHistory.class));
		ArgumentCaptor<HiringEvent> event = ArgumentCaptor.forClass(HiringEvent.class);
		then(events).should().publishEvent(event.capture());
		assertThat(event.getValue().fromStage()).isEqualTo(Stage.APPLIED);
		assertThat(event.getValue().toStage()).isEqualTo(Stage.SCREENING);
	}

	@Test
	void screeningStoresMatchScore() {
		JobApplication application = withId(new JobApplication(job, candidate, null), 30L);
		given(applications.findDetailedById(30L)).willReturn(Optional.of(application));

		service.screen(30L);

		// 2 of 3 skills (46.7) + experience met (30) = 77
		assertThat(application.getMatchScore()).isEqualTo(77);
	}

	private static JobApplication withId(JobApplication application, Long id) {
		ReflectionTestUtils.setField(application, "id", id);
		return application;
	}
}
