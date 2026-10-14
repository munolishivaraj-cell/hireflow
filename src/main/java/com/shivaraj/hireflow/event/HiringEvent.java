package com.shivaraj.hireflow.event;

import java.time.Instant;
import java.util.UUID;

import com.shivaraj.hireflow.application.JobApplication;
import com.shivaraj.hireflow.application.Stage;

/**
 * Message published to Kafka whenever an application is submitted or changes stage. The {@code eventId} lets
 * consumers ignore redelivered messages.
 */
public record HiringEvent(String eventId, Type type, Long applicationId, Long jobId, String jobTitle,
		Long candidateId, String candidateName, String candidateEmail, Stage fromStage, Stage toStage,
		Instant occurredAt) {

	public enum Type {
		APPLICATION_SUBMITTED, STAGE_CHANGED
	}

	public static HiringEvent submitted(JobApplication a) {
		return of(Type.APPLICATION_SUBMITTED, a, null);
	}

	public static HiringEvent stageChanged(JobApplication a, Stage from) {
		return of(Type.STAGE_CHANGED, a, from);
	}

	private static HiringEvent of(Type type, JobApplication a, Stage from) {
		return new HiringEvent(UUID.randomUUID().toString(), type, a.getId(), a.getJob().getId(),
				a.getJob().getTitle(), a.getCandidate().getId(), a.getCandidate().getFullName(),
				a.getCandidate().getEmail(), from, a.getStage(), Instant.now());
	}
}
