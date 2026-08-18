package com.planwith.planwith_fo_grade.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;

public final class Grade {

	private static final int MAX_GRADE_NAME_LENGTH = 50;

	private final Long gradeId;
	private final GradeCode gradeCode;
	private final String gradeName;
	private final int gradeLevel;
	private final String description;
	private final List<GradeCondition> conditions;
	private final List<GradeBenefit> benefits;

	private Grade(
			Long gradeId,
			GradeCode gradeCode,
			String gradeName,
			int gradeLevel,
			String description,
			List<GradeCondition> conditions,
			List<GradeBenefit> benefits
	) {
		this.gradeId = gradeId;
		this.gradeCode = Objects.requireNonNull(gradeCode, "Grade code is required.");
		this.gradeName = requireText(gradeName, MAX_GRADE_NAME_LENGTH, "Grade name is required.");
		if (gradeLevel <= 0) {
			throw new InvalidGradeException("Grade level must be positive.");
		}
		this.gradeLevel = gradeLevel;
		this.description = trimToNull(description);
		this.conditions = copyConditions(conditions);
		this.benefits = copyBenefits(benefits);
	}

	public static Grade create(
			GradeCode gradeCode,
			String gradeName,
			int gradeLevel,
			String description,
			List<GradeCondition> conditions,
			List<GradeBenefit> benefits
	) {
		return new Grade(null, gradeCode, gradeName, gradeLevel, description, conditions, benefits);
	}

	public static Grade reconstitute(
			Long gradeId,
			GradeCode gradeCode,
			String gradeName,
			int gradeLevel,
			String description,
			List<GradeCondition> conditions,
			List<GradeBenefit> benefits
	) {
		Objects.requireNonNull(gradeId, "Grade ID is required.");
		return new Grade(gradeId, gradeCode, gradeName, gradeLevel, description, conditions, benefits);
	}

	public boolean satisfiesAllConditions(java.util.Map<GradeMetricType, Long> metricValues) {
		for (GradeCondition condition : conditions) {
			long currentValue = metricValues.getOrDefault(condition.metricType(), 0L);
			if (!condition.isSatisfiedBy(currentValue)) {
				return false;
			}
		}
		return true;
	}

	public Long gradeId() {
		return gradeId;
	}

	public GradeCode gradeCode() {
		return gradeCode;
	}

	public String gradeName() {
		return gradeName;
	}

	public int gradeLevel() {
		return gradeLevel;
	}

	public String description() {
		return description;
	}

	public List<GradeCondition> conditions() {
		return conditions;
	}

	public List<GradeBenefit> benefits() {
		return benefits;
	}

	private static List<GradeCondition> copyConditions(List<GradeCondition> conditions) {
		if (conditions == null || conditions.isEmpty()) {
			return List.of();
		}
		return Collections.unmodifiableList(new ArrayList<>(conditions));
	}

	private static List<GradeBenefit> copyBenefits(List<GradeBenefit> benefits) {
		if (benefits == null || benefits.isEmpty()) {
			return List.of();
		}
		return Collections.unmodifiableList(new ArrayList<>(benefits));
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
