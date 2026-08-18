package com.planwith.planwith_fo_grade.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grade.cache")
public class GradeCacheProperties {

	private String keyPrefix = "grade:member";
	private Duration ttl = Duration.ofMinutes(10);

	public String getKeyPrefix() { return keyPrefix; }
	public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
	public Duration getTtl() { return ttl; }
	public void setTtl(Duration ttl) { this.ttl = ttl; }

	public String memberKey(String memberUuid) {
		return keyPrefix + ":" + memberUuid;
	}
}
