package com.planwith.planwith_fo_grade.domain.model;

import java.util.Objects;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;

public final class GradeBenefit {

	private static final int MAX_BENEFIT_NAME_LENGTH = 100;
	private static final int MAX_BENEFIT_VALUE_LENGTH = 200;

	private final Long benefitId;
	private final Long gradeId;
	private final BenefitCode benefitCode;
	private final String benefitName;
	private final String benefitValue;
	private final String description;
	private final int sortOrder;

	private GradeBenefit(
			Long benefitId,
			Long gradeId,
			BenefitCode benefitCode,
			String benefitName,
			String benefitValue,
			String description,
			int sortOrder
	) {
		this.benefitId = benefitId;
		this.gradeId = Objects.requireNonNull(gradeId, "Grade ID is required.");
		this.benefitCode = Objects.requireNonNull(benefitCode, "Benefit code is required.");
		this.benefitName = requireText(benefitName, MAX_BENEFIT_NAME_LENGTH, "Benefit name is required.");
		this.benefitValue = requireText(benefitValue, MAX_BENEFIT_VALUE_LENGTH, "Benefit value is required.");
		this.description = trimToNull(description);
		if (sortOrder < 0) {
			throw new InvalidGradeException("Sort order must not be negative.");
		}
		this.sortOrder = sortOrder;
	}

	public static GradeBenefit create(
			Long gradeId,
			BenefitCode benefitCode,
			String benefitName,
			String benefitValue,
			String description,
			int sortOrder
	) {
		return new GradeBenefit(null, gradeId, benefitCode, benefitName, benefitValue, description, sortOrder);
	}

	public static GradeBenefit reconstitute(
			Long benefitId,
			Long gradeId,
			BenefitCode benefitCode,
			String benefitName,
			String benefitValue,
			String description,
			int sortOrder
	) {
		Objects.requireNonNull(benefitId, "Benefit ID is required.");
		return new GradeBenefit(
				benefitId, gradeId, benefitCode, benefitName, benefitValue, description, sortOrder
		);
	}

	public Long benefitId() {
		return benefitId;
	}

	public Long gradeId() {
		return gradeId;
	}

	public BenefitCode benefitCode() {
		return benefitCode;
	}

	public String benefitName() {
		return benefitName;
	}

	public String benefitValue() {
		return benefitValue;
	}

	public String description() {
		return description;
	}

	public int sortOrder() {
		return sortOrder;
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
