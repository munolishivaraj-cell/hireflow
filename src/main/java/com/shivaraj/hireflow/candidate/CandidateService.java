package com.shivaraj.hireflow.candidate;

import java.util.Set;
import java.util.stream.Collectors;

import com.shivaraj.hireflow.common.ApiException;
import com.shivaraj.hireflow.job.JobService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateService {

	private final CandidateRepository candidates;

	public CandidateService(CandidateRepository candidates) {
		this.candidates = candidates;
	}

	@Transactional
	public CandidateResponse register(CandidateRequest request) {
		String email = request.email().trim().toLowerCase();
		if (candidates.existsByEmailIgnoreCase(email)) {
			throw ApiException.conflict("A candidate with email " + email + " already exists");
		}
		Set<String> skills = request.skills().stream().map(JobService::normalizeSkill).collect(Collectors.toSet());
		Candidate candidate = new Candidate(request.fullName().trim(), email, request.phone(),
				request.experienceYears(), skills);
		return CandidateResponse.from(candidates.save(candidate));
	}

	@Transactional(readOnly = true)
	public CandidateResponse get(Long id) {
		return CandidateResponse.from(load(id));
	}

	@Transactional(readOnly = true)
	public Candidate load(Long id) {
		return candidates.findWithSkillsById(id).orElseThrow(() -> ApiException.notFound("Candidate", id));
	}
}
