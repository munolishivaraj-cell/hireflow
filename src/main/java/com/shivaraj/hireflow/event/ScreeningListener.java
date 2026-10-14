package com.shivaraj.hireflow.event;

import com.shivaraj.hireflow.application.ApplicationService;
import com.shivaraj.hireflow.config.KafkaTopics;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Scores every new application asynchronously so the apply request stays fast. */
@Component
public class ScreeningListener {

	private final ApplicationService applicationService;

	public ScreeningListener(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@KafkaListener(topics = KafkaTopics.HIRING_EVENTS, groupId = "hireflow-screening")
	public void onEvent(HiringEvent event) {
		if (event.type() == HiringEvent.Type.APPLICATION_SUBMITTED) {
			applicationService.screen(event.applicationId());
		}
	}
}
