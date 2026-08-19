package com.planwith.planwith_fo_grade.domain.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCondition;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;

/**
 * 현재 등급의 바로 다음 단계와 Metric별 남은 수치·달성률을 계산한다.
 * 승급 평가의 최고 충족 등급 점프와 달리, 화면 Query는 한 단계 위 등급만 대상으로 한다.
 */
public final class GradeProgressCalculator {

	public Optional<Grade> nextGrade(Grade currentGrade, List<Grade> grades) {
		Objects.requireNonNull(currentGrade, "Current grade is required.");
		Objects.requireNonNull(grades, "Grades are required.");
		int nextLevel = currentGrade.gradeLevel() + 1;
		return grades.stream()
				.filter(grade -> grade.gradeLevel() == nextLevel)
				.findFirst();
	}

	public long requiredValue(List<GradeCondition> conditions, GradeMetricType metricType) {
		Objects.requireNonNull(conditions, "Conditions are required.");
		Objects.requireNonNull(metricType, "Metric type is required.");
		return conditions.stream()
				.filter(condition -> condition.metricType() == metricType)
				.mapToLong(GradeCondition::thresholdValue)
				.findFirst()
				.orElse(0L);
	}

	public MetricProgress progress(long currentValue, long requiredValue) {
		long remaining = Math.max(0L, requiredValue - currentValue);
		int percentage = requiredValue <= 0L
				? 100
				: (int) Math.min(100L, (currentValue * 100L) / requiredValue);
		return new MetricProgress(currentValue, requiredValue, remaining, percentage);
	}

	public MetricProgress completed(long currentValue, long requiredValue) {
		return new MetricProgress(currentValue, requiredValue, 0L, 100);
	}
}
