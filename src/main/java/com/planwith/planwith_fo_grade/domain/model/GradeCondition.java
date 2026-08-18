package com.planwith.planwith_fo_grade.domain.model;

import java.util.Objects;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;

public final class GradeCondition {

	private static final int MAX_CONDITION_NAME_LENGTH = 100;

	private final Long conditionId;
	private final Long gradeId;
	private final GradeMetricType metricType;
	private final String conditionName;
	private final long thresholdValue;
	private final int sortOrder;
	private final String description;

	private GradeCondition(
			Long conditionId,
			Long gradeId,
			GradeMetricType metricType,
			String conditionName,
			long thresholdValue,
			int sortOrder,
			String description
	) {
		this.conditionId = conditionId;
		this.gradeId = Objects.requireNonNull(gradeId, "Grade ID is required.");
		this.metricType = Objects.requireNonNull(metricType, "Metric type is required.");
		this.conditionName = requireText(conditionName, MAX_CONDITION_NAME_LENGTH, "Condition name is required.");
		if (thresholdValue < 0) {
			throw new InvalidGradeException("Threshold value must not be negative.");
		}
		this.thresholdValue = thresholdValue;
		if (sortOrder < 0) {
			throw new InvalidGradeException("Sort order must not be negative.");
		}
		this.sortOrder = sortOrder;
		this.description = trimToNull(description);
	}

	public static GradeCondition create(
			Long gradeId,
			GradeMetricType metricType,
			String conditionName,
			long thresholdValue,
			int sortOrder,
			String description
	) {
		return new GradeCondition(null, gradeId, metricType, conditionName, thresholdValue, sortOrder, description);
	}

	public static GradeCondition reconstitute(
			Long conditionId,
			Long gradeId,
			GradeMetricType metricType,
			String conditionName,
			long thresholdValue,
			int sortOrder,
			String description
	) {
		Objects.requireNonNull(conditionId, "Condition ID is required.");
		return new GradeCondition(
				conditionId, gradeId, metricType, conditionName, thresholdValue, sortOrder, description
		);
	}

	public boolean isSatisfiedBy(long currentMetricValue) {
		return currentMetricValue >= thresholdValue;
	}

	public Long conditionId() {
		return conditionId;
	}

	public Long gradeId() {
		return gradeId;
	}

	public GradeMetricType metricType() {
		return metricType;
	}

	public String conditionName() {
		return conditionName;
	}

	public long thresholdValue() {
		return thresholdValue;
	}

	public int sortOrder() {
		return sortOrder;
	}

	public String description() {
		return description;
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
