package com.shivaraj.hireflow.application;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.shivaraj.hireflow.candidate.Candidate;
import com.shivaraj.hireflow.candidate.CandidateService;
import com.shivaraj.hireflow.common.ApiException;
import com.shivaraj.hireflow.config.CacheNames;
import com.shivaraj.hireflow.event.HiringEvent;
import com.shivaraj.hireflow.job.Job;
import com.shivaraj.hireflow.job.JobService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

	private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

	private final ApplicationRepository applications;

	private final StageHistoryRepository history;

	private final JobService jobService;

	private final CandidateService candidateService;

	private final ApplyRateLimiter rateLimiter;

	private final MatchScoreCalculator scoreCalculator;

	private final ApplicationEventPublisher events;

	private final CacheManager cacheManager;

	public ApplicationService(ApplicationRepository applications, StageHistoryRepository history, JobService jobService,
			CandidateService candidateService, ApplyRateLimiter rateLimiter, MatchScoreCalculator scoreCalculator,
			ApplicationEventPublisher events, CacheManager cacheManager) {
		this.applications = applications;
		this.history = history;
		this.jobService = jobService;
		this.candidateService = candidateService;
		this.rateLimiter = rateLimiter;
		this.scoreCalculator = scoreCalculator;
		this.events = events;
		this.cacheManager = cacheManager;
	}

	@Transactional
	public ApplicationResponse apply(ApplyRequest request) {
		rateLimiter.check(request.candidateId());

		Job job = jobService.load(request.jobId());
		if (!job.isOpen()) {
			throw ApiException.conflict("Job " + job.getId() + " is closed for applications");
		}
		Candidate candidate = candidateService.load(request.candidateId());
		if (applications.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
			throw ApiException.conflict("Candidate " + candidate.getId() + " already applied to job " + job.getId());
		}

		JobApplication application = applications.save(new JobApplication(job, candidate, request.coverNote()));
		history.save(new StageHistory(application.getId(), null, Stage.APPLIED, "Application submitted"));

		// Delivered to Kafka only after this transaction commits (see HiringEventRelay).
		events.publishEvent(HiringEvent.submitted(application));
		evictPipeline(job.getId());
		return ApplicationResponse.from(application);
	}

	@Transactional
	public ApplicationResponse changeStage(Long applicationId, StageChangeRequest request) {
		JobApplication application = loadDetailed(applicationId);
		Stage previous = application.moveTo(request.stage());
		history.save(new StageHistory(application.getId(), previous, request.stage(), request.note()));

		events.publishEvent(HiringEvent.stageChanged(application, previous));
		evictPipeline(application.getJob().getId());
		return ApplicationResponse.from(application);
	}

	/** Called by the screening consumer: computes how well the candidate fits the job and stores it. */
	@Transactional
	public void screen(Long applicationId) {
		JobApplication application = loadDetailed(applicationId);
		int score = scoreCalculator.score(application.getJob(), application.getCandidate());
		application.recordMatchScore(score);
		log.info("Screened application {} with match score {}", applicationId, score);
	}

	@Transactional(readOnly = true)
	public ApplicationResponse get(Long applicationId) {
		return ApplicationResponse.from(loadDetailed(applicationId));
	}

	@Transactional(readOnly = true)
	public List<StageHistoryResponse> history(Long applicationId) {
		loadDetailed(applicationId);
		return history.findByApplicationIdOrderByChangedAtAsc(applicationId)
			.stream()
			.map(StageHistoryResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<ApplicationResponse> forCandidate(Long candidateId) {
		candidateService.load(candidateId);
		return applications.findByCandidateIdOrderByCreatedAtDesc(candidateId)
			.stream()
			.map(ApplicationResponse::from)
			.toList();
	}

	/** Applications for a job, best match first, optionally filtered by stage. */
	@Transactional(readOnly = true)
	public List<ApplicationResponse> rankedForJob(Long jobId, Stage stage) {
		jobService.load(jobId);
		return applications.findRankedForJob(jobId, stage).stream().map(ApplicationResponse::from).toList();
	}

	@Cacheable(cacheNames = CacheNames.PIPELINE, key = "#jobId")
	@Transactional(readOnly = true)
	public PipelineSummary pipeline(Long jobId) {
		jobService.load(jobId);
		Map<Stage, Long> byStage = new EnumMap<>(Stage.class);
		for (Stage stage : Stage.values()) {
			byStage.put(stage, 0L);
		}
		long total = 0;
		for (ApplicationRepository.StageCount count : applications.countByStage(jobId)) {
			byStage.put(count.getStage(), count.getTotal());
			total += count.getTotal();
		}
		return new PipelineSummary(jobId, total, byStage);
	}

	private JobApplication loadDetailed(Long applicationId) {
		return applications.findDetailedById(applicationId)
			.orElseThrow(() -> ApiException.notFound("Application", applicationId));
	}

	private void evictPipeline(Long jobId) {
		Cache cache = cacheManager.getCache(CacheNames.PIPELINE);
		if (cache != null) {
			cache.evict(jobId);
		}
	}
}
