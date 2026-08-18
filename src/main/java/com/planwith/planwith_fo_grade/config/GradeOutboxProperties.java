package com.planwith.planwith_fo_grade.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grade.outbox")
public class GradeOutboxProperties {

	private boolean enabled = true;
	private int relayBatchSize = 50;
	private Duration sendTimeout = Duration.ofSeconds(10);

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public int getRelayBatchSize() { return relayBatchSize; }
	public void setRelayBatchSize(int relayBatchSize) { this.relayBatchSize = relayBatchSize; }
	public Duration getSendTimeout() { return sendTimeout; }
	public void setSendTimeout(Duration sendTimeout) { this.sendTimeout = sendTimeout; }
}
