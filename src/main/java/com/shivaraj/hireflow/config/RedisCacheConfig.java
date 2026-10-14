package com.shivaraj.hireflow.config;

import java.time.Duration;

import com.shivaraj.hireflow.application.PipelineSummary;
import com.shivaraj.hireflow.job.JobResponse;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

/**
 * Each cache gets its own TTL and a JSON serializer for its value type, so cached entries are readable in
 * redis-cli and survive class changes better than JDK serialization.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisCacheConfig {

	@Bean
	RedisCacheManagerBuilderCustomizer hireflowCaches() {
		return builder -> builder
			.withCacheConfiguration(CacheNames.JOBS, cacheFor(JobResponse.class, Duration.ofMinutes(10)))
			.withCacheConfiguration(CacheNames.PIPELINE, cacheFor(PipelineSummary.class, Duration.ofSeconds(30)));
	}

	private static <T> RedisCacheConfiguration cacheFor(Class<T> type, Duration ttl) {
		return RedisCacheConfiguration.defaultCacheConfig()
			.prefixCacheNameWith("hireflow:")
			.entryTtl(ttl)
			.disableCachingNullValues()
			.serializeValuesWith(SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(type)));
	}
}
