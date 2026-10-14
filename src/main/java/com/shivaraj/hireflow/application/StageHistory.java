package com.shivaraj.hireflow.application;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Append-only audit trail of every stage change. */
@Entity
@Table(name = "application_stage_history")
public class StageHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_id")
	private Long applicationId;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_stage")
	private Stage fromStage;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_stage")
	private Stage toStage;

	private String note;

	@Column(name = "changed_at")
	private Instant changedAt;

	protected StageHistory() {
	}

	public StageHistory(Long applicationId, Stage fromStage, Stage toStage, String note) {
		this.applicationId = applicationId;
		this.fromStage = fromStage;
		this.toStage = toStage;
		this.note = note;
		this.changedAt = Instant.now();
	}

	public Stage getFromStage() {
		return fromStage;
	}

	public Stage getToStage() {
		return toStage;
	}

	public String getNote() {
		return note;
	}

	public Instant getChangedAt() {
		return changedAt;
	}
}
