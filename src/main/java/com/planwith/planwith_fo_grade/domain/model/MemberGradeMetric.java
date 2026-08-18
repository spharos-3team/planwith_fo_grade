package com.planwith.planwith_fo_grade.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

public final class MemberGradeMetric {

	private static final int MAX_SOURCE_SERVICE_LENGTH = 50;

	private final Long metricId;
	private final MemberUuid memberUuid;
	private final MemberMetricType metricType;
	private final long currentValue;
	private final String sourceService;
	private final long sourceVersion;
	private final LocalDateTime synchronizedAt;

	private MemberGradeMetric(
			Long metricId,
			MemberUuid memberUuid,
			MemberMetricType metricType,
			long currentValue,
			String sourceService,
			long sourceVersion,
			LocalDateTime synchronizedAt
	) {
		this.metricId = metricId;
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.metricType = Objects.requireNonNull(metricType, "Metric type is required.");
		if (currentValue < 0) {
			throw new InvalidGradeException("Current metric value must not be negative.");
		}
		this.currentValue = currentValue;
		this.sourceService = requireText(sourceService, MAX_SOURCE_SERVICE_LENGTH, "Source service is required.");
		if (sourceVersion < 0) {
			throw new InvalidGradeException("Source version must not be negative.");
		}
		this.sourceVersion = sourceVersion;
		this.synchronizedAt = Objects.requireNonNull(synchronizedAt, "Synchronized at is required.");
	}

	public static MemberGradeMetric initialize(
			MemberUuid memberUuid,
			MemberMetricType metricType,
			String sourceService,
			LocalDateTime synchronizedAt
	) {
		return new MemberGradeMetric(null, memberUuid, metricType, 0L, sourceService, 0L, synchronizedAt);
	}

	public static MemberGradeMetric reconstitute(
			Long metricId,
			MemberUuid memberUuid,
			MemberMetricType metricType,
			long currentValue,
			String sourceService,
			long sourceVersion,
			LocalDateTime synchronizedAt
	) {
		Objects.requireNonNull(metricId, "Metric ID is required.");
		return new MemberGradeMetric(
				metricId, memberUuid, metricType, currentValue, sourceService, sourceVersion, synchronizedAt
		);
	}

	public MemberGradeMetric applyDelta(long delta, String sourceService, long sourceVersion, LocalDateTime synchronizedAt) {
		return synchronize(currentValue + delta, sourceService, sourceVersion, synchronizedAt);
	}

	public MemberGradeMetric synchronize(
			long newValue,
			String sourceService,
			long sourceVersion,
			LocalDateTime synchronizedAt
	) {
		if (sourceVersion < this.sourceVersion) {
			return this;
		}
		if (newValue < 0) {
			throw new InvalidGradeException("Synchronized metric value must not be negative.");
		}
		return new MemberGradeMetric(
				metricId, memberUuid, metricType, newValue, sourceService, sourceVersion, synchronizedAt
		);
	}

	public Long metricId() {
		return metricId;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public MemberMetricType metricType() {
		return metricType;
	}

	public long currentValue() {
		return currentValue;
	}

	public String sourceService() {
		return sourceService;
	}

	public long sourceVersion() {
		return sourceVersion;
	}

	public LocalDateTime synchronizedAt() {
		return synchronizedAt;
	}

	private static String requireText(String value, int maxLength, String message) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			throw new InvalidGradeException(message);
		}
		if (trimmed.length() > maxLength) {
			throw new InvalidGradeException("Text exceeds max length " + maxLength + ".");
		}
		return trimmed;
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
