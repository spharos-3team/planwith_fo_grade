package com.planwith.planwith_fo_grade.application.query;

public record GradeConditionView(
		String metricType,
		String conditionName,
		long thresholdValue,
		String description
) {
}
