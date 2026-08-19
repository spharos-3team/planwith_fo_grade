package com.planwith.planwith_fo_grade.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;

class GradeProgressCalculatorTest {

	private final GradeProgressCalculator calculator = new GradeProgressCalculator();
	private final List<Grade> grades = GradeCriteriaCatalog.initialGrades();

	@ParameterizedTest(name = "{0} → {1}")
	@CsvSource({
			"ROOKIE, LEAF",
			"LEAF, TRAVELER",
			"TRAVELER, EXPLORER",
			"EXPLORER, ADVENTURE",
			"ADVENTURE, PLANWITH"
	})
	void returnsImmediateNextGrade(GradeCode currentGrade, GradeCode expectedNextGrade) {
		assertThat(calculator.nextGrade(grade(currentGrade), grades).orElseThrow().gradeCode())
				.isEqualTo(expectedNextGrade);
	}

	@Test
	void returnsImmediateNextGradeInsteadOfHighestSatisfiedGrade() {
		Grade next = calculator.nextGrade(grade(GradeCode.LEAF), grades).orElseThrow();

		assertThat(next.gradeCode()).isEqualTo(GradeCode.TRAVELER);
		assertThat(calculator.requiredValue(next.conditions(), GradeMetricType.STORY_COUNT)).isEqualTo(10L);
		assertThat(calculator.requiredValue(next.conditions(), GradeMetricType.FOLLOWER_COUNT)).isEqualTo(100L);
		assertThat(calculator.requiredValue(next.conditions(), GradeMetricType.RECEIVED_LIKE_COUNT)).isEqualTo(500L);
	}

	@Test
	void returnsEmptyNextGradeForHighestGrade() {
		assertThat(calculator.nextGrade(grade(GradeCode.PLANWITH), grades)).isEmpty();
	}

	@Test
	void calculatesRemainingAndPercentageForLeafToTravelerExample() {
		assertThat(calculator.progress(7L, 10L)).isEqualTo(new MetricProgress(7L, 10L, 3L, 70));
		assertThat(calculator.progress(62L, 100L)).isEqualTo(new MetricProgress(62L, 100L, 38L, 62));
		assertThat(calculator.progress(410L, 500L)).isEqualTo(new MetricProgress(410L, 500L, 90L, 82));
	}

	@Test
	void calculatesAchievementRateAndCapsAtOneHundredPercent() {
		assertThat(calculator.progress(7L, 10L).percentage()).isEqualTo(70);
		assertThat(calculator.progress(50L, 100L).percentage()).isEqualTo(50);
		assertThat(calculator.progress(15L, 10L)).isEqualTo(new MetricProgress(15L, 10L, 0L, 100));
	}

	@Test
	void capsPercentageAndRemainingWhenCurrentExceedsRequired() {
		assertThat(calculator.progress(15L, 10L)).isEqualTo(new MetricProgress(15L, 10L, 0L, 100));
	}

	@Test
	void treatsZeroRequiredAsCompleted() {
		assertThat(calculator.progress(0L, 0L)).isEqualTo(new MetricProgress(0L, 0L, 0L, 100));
	}

	@Test
	void completedProgressKeepsCurrentAndRequiredButZeroRemaining() {
		assertThat(calculator.completed(180L, 200L)).isEqualTo(new MetricProgress(180L, 200L, 0L, 100));
	}

	private Grade grade(GradeCode gradeCode) {
		return grades.stream()
				.filter(candidate -> candidate.gradeCode() == gradeCode)
				.findFirst()
				.orElseThrow();
	}
}
