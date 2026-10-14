package com.shivaraj.hireflow.application;

import java.time.Instant;

public record ApplicationResponse(Long id, Long jobId, String jobTitle, Long candidateId, String candidateName,
		Stage stage, Integer matchScore, String coverNote, Instant createdAt, Instant updatedAt) {

	static ApplicationResponse from(JobApplication a) {
		return new ApplicationResponse(a.getId(), a.getJob().getId(), a.getJob().getTitle(), a.getCandidate().getId(),
				a.getCandidate().getFullName(), a.getStage(), a.getMatchScore(), a.getCoverNote(), a.getCreatedAt(),
				a.getUpdatedAt());
	}
}
