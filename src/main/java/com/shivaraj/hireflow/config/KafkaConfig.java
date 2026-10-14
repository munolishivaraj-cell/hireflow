package com.shivaraj.hireflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration(proxyBeanMethods = false)
public class KafkaConfig {

	/** Created on startup for local runs; on Aiven the topics are created in the console instead. */
	@Bean
	NewTopic hiringEventsTopic(@Value("${hireflow.kafka.partitions:3}") int partitions) {
		return TopicBuilder.name(KafkaTopics.HIRING_EVENTS).partitions(partitions).replicas(1).build();
	}

	@Bean
	NewTopic hiringEventsDeadLetterTopic(@Value("${hireflow.kafka.partitions:3}") int partitions) {
		return TopicBuilder.name(KafkaTopics.HIRING_EVENTS_DLT).partitions(partitions).replicas(1).build();
	}

	/**
	 * Retries a failing record three times with exponential backoff, then parks it on the dead-letter topic
	 * so one bad message cannot block its partition. Picked up automatically by Boot's listener container
	 * factory.
	 */
	@Bean
	CommonErrorHandler kafkaErrorHandler(KafkaOperations<Object, Object> template) {
		ExponentialBackOff backOff = new ExponentialBackOff(500, 2.0);
		backOff.setMaxAttempts(3);
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
				(record, ex) -> new TopicPartition(KafkaTopics.HIRING_EVENTS_DLT, record.partition()));
		return new DefaultErrorHandler(recoverer, backOff);
	}
}
