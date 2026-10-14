package com.shivaraj.hireflow.candidate;

import java.time.Instant;
import java.util.List;

public record CandidateResponse(Long id, String fullName, String email, String phone, int experienceYears,
		List<String> skills, Instant createdAt) {

	static CandidateResponse from(Candidate c) {
		return new CandidateResponse(c.getId(), c.getFullName(), c.getEmail(), c.getPhone(), c.getExperienceYears(),
				List.copyOf(c.getSkills()), c.getCreatedAt());
	}
}
