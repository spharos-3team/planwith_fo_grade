package com.planwith.planwith_fo_grade.domain.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;

/**
 * 회원 Metric과 등급 조건(AND)을 비교해 충족하는 가장 높은 등급을 계산한다.
 * 승급만 반환하며, 현재 등급보다 낮은 결과는 승급 대상으로 보지 않는다.
 */
public final class GradeEvaluator {

	public Optional<Grade> highestSatisfiedGrade(List<Grade> grades, Map<GradeMetricType, Long> metricValues) {
		Objects.requireNonNull(grades, "Grades are required.");
		Objects.requireNonNull(metricValues, "Metric values are required.");
		return grades.stream()
				.sorted(Comparator.comparingInt(Grade::gradeLevel).reversed())
				.filter(grade -> grade.satisfiesAllConditions(metricValues))
				.findFirst();
	}

	public Optional<Grade> promotionTarget(Grade currentGrade, Grade highestSatisfiedGrade) {
		Objects.requireNonNull(currentGrade, "Current grade is required.");
		Objects.requireNonNull(highestSatisfiedGrade, "Highest satisfied grade is required.");
		if (highestSatisfiedGrade.gradeLevel() > currentGrade.gradeLevel()) {
			return Optional.of(highestSatisfiedGrade);
		}
		return Optional.empty();
	}
}
