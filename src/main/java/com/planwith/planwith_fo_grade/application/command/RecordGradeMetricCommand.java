package com.planwith.planwith_fo_grade.application.command;

public record RecordGradeMetricCommand(
		String memberUuid,
		String metricType,
		long delta
) {
}
