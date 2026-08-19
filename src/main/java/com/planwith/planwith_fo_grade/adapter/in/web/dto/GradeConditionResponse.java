package com.planwith.planwith_fo_grade.adapter.in.web.dto;

public record GradeConditionResponse(
		String metricType,
		String conditionName,
		long thresholdValue,
		String description
) {
}
