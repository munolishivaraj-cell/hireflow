package com.shivaraj.hireflow.notification;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A message sent to a candidate. Stored instead of emailed so the flow can be inspected through the API. */
@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id")
	private String eventId;

	@Column(name = "candidate_id")
	private Long candidateId;

	@Column(name = "application_id")
	private Long applicationId;

	private String recipient;

	private String subject;

	private String body;

	@Column(name = "created_at")
	private Instant createdAt;

	protected Notification() {
	}

	public Notification(String eventId, Long candidateId, Long applicationId, String recipient, String subject,
			String body) {
		this.eventId = eventId;
		this.candidateId = candidateId;
		this.applicationId = applicationId;
		this.recipient = recipient;
		this.subject = subject;
		this.body = body;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
