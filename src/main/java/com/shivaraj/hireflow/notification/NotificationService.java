package com.shivaraj.hireflow.notification;

import java.util.List;

import com.shivaraj.hireflow.event.HiringEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository notifications;

	public NotificationService(NotificationRepository notifications) {
		this.notifications = notifications;
	}

	/**
	 * Idempotent: Kafka delivers at least once, so a redelivered event is skipped by its id (backed by a unique
	 * constraint on {@code notifications.event_id}).
	 */
	@Transactional
	public void notifyCandidate(HiringEvent event) {
		if (notifications.existsByEventId(event.eventId())) {
			log.info("Skipping duplicate event {}", event.eventId());
			return;
		}
		String subject = subjectFor(event);
		String body = "Hi " + event.candidateName() + ", " + bodyFor(event);
		notifications.save(new Notification(event.eventId(), event.candidateId(), event.applicationId(),
				event.candidateEmail(), subject, body));
		log.info("Notified {} about application {}: {}", event.candidateEmail(), event.applicationId(), subject);
	}

	@Transactional(readOnly = true)
	public List<NotificationResponse> forCandidate(Long candidateId) {
		return notifications.findByCandidateIdOrderByCreatedAtDesc(candidateId)
			.stream()
			.map(NotificationResponse::from)
			.toList();
	}

	private static String subjectFor(HiringEvent e) {
		if (e.type() == HiringEvent.Type.APPLICATION_SUBMITTED) {
			return "Application received: " + e.jobTitle();
		}
		return "Update on your application: " + e.jobTitle();
	}

	private static String bodyFor(HiringEvent e) {
		if (e.type() == HiringEvent.Type.APPLICATION_SUBMITTED) {
			return "we received your application for " + e.jobTitle() + ".";
		}
		return switch (e.toStage()) {
			case SCREENING -> "your profile is being reviewed by our recruiting team.";
			case INTERVIEW -> "we'd like to invite you to interview for " + e.jobTitle() + ".";
			case OFFER -> "congratulations, we're preparing an offer for " + e.jobTitle() + ".";
			case HIRED -> "welcome aboard!";
			case REJECTED -> "thank you for your interest. We won't be moving forward this time.";
			case WITHDRAWN -> "your application has been withdrawn.";
			case APPLIED -> "your application status was updated.";
		};
	}
}
