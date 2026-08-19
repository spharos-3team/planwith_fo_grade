package com.planwith.planwith_fo_grade.application.command;

import java.util.Objects;

import com.planwith.planwith_fo_grade.domain.event.MetricEventType;

public final class RecordGradeMetricCommandFactory {

	private RecordGradeMetricCommandFactory() {
	}

	public static RecordGradeMetricCommand from(MetricEventType eventType, String metricOwnerUuid) {
		Objects.requireNonNull(eventType, "Metric event type is required.");
		String ownerUuid = requireText(metricOwnerUuid, "Metric owner UUID is required.");
		return new RecordGradeMetricCommand(
				ownerUuid,
				eventType.metricType().name(),
				eventType.delta()
		);
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}
}
