package com.shivaraj.hireflow.job;

import java.time.Instant;
import java.util.List;

public record JobResponse(Long id, String title, String department, String location, int minExperienceYears,
		String description, JobStatus status, List<String> skills, Instant createdAt) {

	static JobResponse from(Job job) {
		return new JobResponse(job.getId(), job.getTitle(), job.getDepartment(), job.getLocation(),
				job.getMinExperienceYears(), job.getDescription(), job.getStatus(), List.copyOf(job.getSkills()),
				job.getCreatedAt());
	}
}
