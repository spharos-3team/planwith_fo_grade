package com.planwith.planwith_fo_grade.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class GradeOutboxPropertiesTest {

	@Test
	void usesExponentialBackoffUntilMaxInterval() {
		GradeOutboxProperties properties = new GradeOutboxProperties();
		properties.setBackoffInitial(Duration.ofSeconds(5));
		properties.setBackoffMultiplier(2.0d);
		properties.setBackoffMax(Duration.ofMinutes(5));
		properties.setMaxRetry(10);

		assertThat(properties.retryDelay(1)).isEqualTo(Duration.ofSeconds(5));
		assertThat(properties.retryDelay(2)).isEqualTo(Duration.ofSeconds(10));
		assertThat(properties.retryDelay(3)).isEqualTo(Duration.ofSeconds(20));
		assertThat(properties.retryDelay(10)).isEqualTo(Duration.ofMinutes(5));
		assertThat(properties.retryLimitReached(9)).isFalse();
		assertThat(properties.retryLimitReached(10)).isTrue();
		assertThat(properties.nextRetryAt(Instant.parse("2026-08-19T06:00:00Z"), 1))
				.isEqualTo(Instant.parse("2026-08-19T06:00:05Z"));
	}
}
