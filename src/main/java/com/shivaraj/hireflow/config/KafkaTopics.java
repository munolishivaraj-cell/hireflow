package com.shivaraj.hireflow.config;

public final class KafkaTopics {

	public static final String HIRING_EVENTS = "hireflow.hiring-events";

	/** Records that still fail after retries are parked here (same partition as the original). */
	public static final String HIRING_EVENTS_DLT = HIRING_EVENTS + ".DLT";

	private KafkaTopics() {
	}
}
