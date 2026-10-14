package com.shivaraj.hireflow.application;

import java.time.Duration;

import com.shivaraj.hireflow.common.ApiException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Fixed-window rate limit on applications per candidate, backed by Redis so the limit holds across every
 * running instance of the service.
 */
@Component
public class ApplyRateLimiter {

	private final StringRedisTemplate redis;

	private final int maxApplications;

	private final Duration window;

	public ApplyRateLimiter(StringRedisTemplate redis, @Value("${hireflow.apply-limit.max:5}") int maxApplications,
			@Value("${hireflow.apply-limit.window:1h}") Duration window) {
		this.redis = redis;
		this.maxApplications = maxApplications;
		this.window = window;
	}

	public void check(Long candidateId) {
		String key = "hireflow:rate:apply:" + candidateId;
		Long count = redis.opsForValue().increment(key);
		if (count != null && count == 1) {
			redis.expire(key, window);
		}
		if (count != null && count > maxApplications) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Candidate " + candidateId + " may submit at most "
					+ maxApplications + " applications per " + window.toMinutes() + " minutes");
		}
	}
}
