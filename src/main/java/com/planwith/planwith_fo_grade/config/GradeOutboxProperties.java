package com.planwith.planwith_fo_grade.config;

import java.time.Duration;
import java.time.Instant;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grade.outbox")
public class GradeOutboxProperties {

	private boolean enabled = true;
	private int relayBatchSize = 50;
	private Duration sendTimeout = Duration.ofSeconds(10);
	private int maxRetry = 10;
	private Duration backoffInitial = Duration.ofSeconds(5);
	private double backoffMultiplier = 2.0d;
	private Duration backoffMax = Duration.ofMinutes(5);

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public int getRelayBatchSize() { return relayBatchSize; }
	public void setRelayBatchSize(int relayBatchSize) { this.relayBatchSize = relayBatchSize; }
	public Duration getSendTimeout() { return sendTimeout; }
	public void setSendTimeout(Duration sendTimeout) { this.sendTimeout = sendTimeout; }
	public int getMaxRetry() { return maxRetry; }
	public void setMaxRetry(int maxRetry) { this.maxRetry = maxRetry; }
	public Duration getBackoffInitial() { return backoffInitial; }
	public void setBackoffInitial(Duration backoffInitial) { this.backoffInitial = backoffInitial; }
	public double getBackoffMultiplier() { return backoffMultiplier; }
	public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }
	public Duration getBackoffMax() { return backoffMax; }
	public void setBackoffMax(Duration backoffMax) { this.backoffMax = backoffMax; }

	public int resolvedMaxRetry() {
		return maxRetry > 0 ? maxRetry : 10;
	}

	public boolean retryLimitReached(int retryCount) {
		return retryCount >= resolvedMaxRetry();
	}

	public Duration retryDelay(int retryCount) {
		Duration initial = positive(backoffInitial, Duration.ofSeconds(5));
		Duration max = positive(backoffMax, Duration.ofMinutes(5));
		if (retryLimitReached(retryCount)) {
			return max;
		}
		double multiplier = backoffMultiplier < 1.0d ? 2.0d : backoffMultiplier;
		int attempts = Math.max(1, retryCount);
		long millis = (long) (initial.toMillis() * Math.pow(multiplier, attempts - 1));
		return Duration.ofMillis(Math.min(max.toMillis(), Math.max(initial.toMillis(), millis)));
	}

	public Instant nextRetryAt(Instant now, int retryCount) {
		return now.plus(retryDelay(retryCount));
	}

	private static Duration positive(Duration value, Duration fallback) {
		if (value == null || value.isZero() || value.isNegative()) {
			return fallback;
		}
		return value;
	}
}
