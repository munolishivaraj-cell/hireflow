package com.shivaraj.hireflow.event;

import com.shivaraj.hireflow.config.KafkaTopics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Forwards hiring events to Kafka once the database transaction has committed, so consumers never see an
 * application that was rolled back. The application id is the message key, which keeps all events for one
 * application on the same partition and therefore in order.
 */
@Component
public class HiringEventRelay {

	private static final Logger log = LoggerFactory.getLogger(HiringEventRelay.class);

	private final KafkaTemplate<String, HiringEvent> kafka;

	public HiringEventRelay(KafkaTemplate<String, HiringEvent> kafka) {
		this.kafka = kafka;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void relay(HiringEvent event) {
		kafka.send(KafkaTopics.HIRING_EVENTS, event.applicationId().toString(), event).whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("Failed to publish {} for application {}", event.type(), event.applicationId(), ex);
			}
			else {
				log.debug("Published {} to partition {} offset {}", event.type(),
						result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
			}
		});
	}
}
