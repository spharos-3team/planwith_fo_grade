package com.planwith.planwith_fo_grade.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

public final class ProcessedGradeEvent {

	private final UUID eventUuid;
	private final MemberUuid memberUuid;
	private final MemberMetricType metricType;
	private final LocalDateTime processedAt;

	private ProcessedGradeEvent(
			UUID eventUuid,
			MemberUuid memberUuid,
			MemberMetricType metricType,
			LocalDateTime processedAt
	) {
		this.eventUuid = Objects.requireNonNull(eventUuid, "Event UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.metricType = Objects.requireNonNull(metricType, "Metric type is required.");
		this.processedAt = Objects.requireNonNull(processedAt, "Processed at is required.");
	}

	public static ProcessedGradeEvent recorded(
			UUID eventUuid,
			MemberUuid memberUuid,
			MemberMetricType metricType,
			LocalDateTime processedAt
	) {
		return new ProcessedGradeEvent(eventUuid, memberUuid, metricType, processedAt);
	}

	public UUID eventUuid() {
		return eventUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public MemberMetricType metricType() {
		return metricType;
	}

	public LocalDateTime processedAt() {
		return processedAt;
	}
}
