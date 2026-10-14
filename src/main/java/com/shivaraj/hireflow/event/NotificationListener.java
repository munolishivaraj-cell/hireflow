package com.shivaraj.hireflow.event;

import com.shivaraj.hireflow.config.KafkaTopics;
import com.shivaraj.hireflow.notification.NotificationService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Separate consumer group from screening, so each concern receives every event and can fail or lag
 * independently.
 */
@Component
public class NotificationListener {

	private final NotificationService notificationService;

	public NotificationListener(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@KafkaListener(topics = KafkaTopics.HIRING_EVENTS, groupId = "hireflow-notifications")
	public void onEvent(HiringEvent event) {
		notificationService.notifyCandidate(event);
	}
}
