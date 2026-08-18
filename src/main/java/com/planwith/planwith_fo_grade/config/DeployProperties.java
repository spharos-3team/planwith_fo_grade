package com.planwith.planwith_fo_grade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.deploy")
public record DeployProperties(
		String marker
) {
}
