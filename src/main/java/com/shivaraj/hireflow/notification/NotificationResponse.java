package com.shivaraj.hireflow.notification;

import java.time.Instant;

public record NotificationResponse(Long id, Long applicationId, String recipient, String subject, String body,
		Instant createdAt) {

	static NotificationResponse from(Notification n) {
		return new NotificationResponse(n.getId(), n.getApplicationId(), n.getRecipient(), n.getSubject(),
				n.getBody(), n.getCreatedAt());
	}
}
