package com.planwith.planwith_fo_grade.application.command;

import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_grade.domain.event.MetricEventType;

public final class RecordGradeMetricCommandFactory {

	private RecordGradeMetricCommandFactory() {
	}

	public static RecordGradeMetricCommand from(MetricEventType eventType, String metricOwnerUuid, String eventUuid) {
		return from(eventType, metricOwnerUuid, eventUuid, null);
	}

	public static RecordGradeMetricCommand from(
			MetricEventType eventType,
			String metricOwnerUuid,
			String eventUuid,
			Long sourceVersion
	) {
		Objects.requireNonNull(eventType, "Metric event type is required.");
		String ownerUuid = requireText(metricOwnerUuid, "Metric owner UUID is required.");
		String processedEventUuid = requireEventUuid(eventUuid);
		if (sourceVersion != null && sourceVersion < 1L) {
			throw new IllegalArgumentException("Source version must be positive.");
		}
		return new RecordGradeMetricCommand(
				processedEventUuid,
				ownerUuid,
				eventType.metricType().name(),
				eventType.delta(),
				sourceVersion
		);
	}

	private static String requireEventUuid(String eventUuid) {
		String value = requireText(eventUuid, "Event UUID is required.");
		try {
			return UUID.fromString(value).toString();
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Event UUID is invalid.");
		}
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}
}
