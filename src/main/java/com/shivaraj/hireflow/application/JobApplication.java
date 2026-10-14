package com.shivaraj.hireflow.application;

import java.time.Instant;

import com.shivaraj.hireflow.candidate.Candidate;
import com.shivaraj.hireflow.common.ApiException;
import com.shivaraj.hireflow.job.Job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.springframework.http.HttpStatus;

@Entity
@Table(name = "applications")
public class JobApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_id")
	private Job job;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "candidate_id")
	private Candidate candidate;

	@Enumerated(EnumType.STRING)
	private Stage stage = Stage.APPLIED;

	@Column(name = "cover_note")
	private String coverNote;

	/** Filled in asynchronously by the screening consumer; null until then. */
	@Column(name = "match_score")
	private Integer matchScore;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@Version
	private long version;

	protected JobApplication() {
	}

	public JobApplication(Job job, Candidate candidate, String coverNote) {
		this.job = job;
		this.candidate = candidate;
		this.coverNote = coverNote;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	/** Moves to {@code target} and returns the previous stage. */
	public Stage moveTo(Stage target) {
		if (!stage.canMoveTo(target)) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
					"Cannot move application " + id + " from " + stage + " to " + target);
		}
		Stage previous = stage;
		stage = target;
		return previous;
	}

	public void recordMatchScore(int score) {
		this.matchScore = score;
	}

	public Long getId() {
		return id;
	}

	public Job getJob() {
		return job;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public Stage getStage() {
		return stage;
	}

	public String getCoverNote() {
		return coverNote;
	}

	public Integer getMatchScore() {
		return matchScore;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
