package com.shivaraj.hireflow.job;

import java.util.Set;
import java.util.stream.Collectors;

import com.shivaraj.hireflow.common.ApiException;
import com.shivaraj.hireflow.common.PageResponse;
import com.shivaraj.hireflow.config.CacheNames;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

	private final JobRepository jobs;

	public JobService(JobRepository jobs) {
		this.jobs = jobs;
	}

	@Transactional
	public JobResponse create(JobRequest request) {
		Set<String> skills = request.skills().stream().map(JobService::normalizeSkill).collect(Collectors.toSet());
		Job job = new Job(request.title(), request.department(), request.location(), request.minExperienceYears(),
				request.description(), skills);
		return JobResponse.from(jobs.save(job));
	}

	/** Job details are read far more often than written, so they are cached in Redis. */
	@Cacheable(cacheNames = CacheNames.JOBS, key = "#id")
	@Transactional(readOnly = true)
	public JobResponse get(Long id) {
		return JobResponse.from(load(id));
	}

	@Transactional(readOnly = true)
	public PageResponse<JobResponse> searchOpen(String skill, int page, int size) {
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		String normalized = (skill == null || skill.isBlank()) ? null : normalizeSkill(skill);
		return PageResponse.of(jobs.search(JobStatus.OPEN, normalized, pageable).map(JobResponse::from));
	}

	@CacheEvict(cacheNames = CacheNames.JOBS, key = "#id")
	@Transactional
	public JobResponse close(Long id) {
		Job job = load(id);
		job.close();
		return JobResponse.from(job);
	}

	@Transactional(readOnly = true)
	public Job load(Long id) {
		return jobs.findWithSkillsById(id).orElseThrow(() -> ApiException.notFound("Job", id));
	}

	public static String normalizeSkill(String skill) {
		return skill.trim().toLowerCase();
	}
}
