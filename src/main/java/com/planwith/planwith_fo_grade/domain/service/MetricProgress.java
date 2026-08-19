package com.planwith.planwith_fo_grade.domain.service;

public record MetricProgress(
		long current,
		long required,
		long remaining,
		int percentage
) {
}
