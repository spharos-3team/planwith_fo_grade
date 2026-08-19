package com.planwith.planwith_fo_grade.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;

class GradeEvaluatorTest {

	private final GradeEvaluator evaluator = new GradeEvaluator();
	private final List<Grade> grades = GradeCriteriaCatalog.initialGrades();

	@Test
	void selectsHighestSatisfiedGradeByAndThresholds() {
		Grade highest = evaluator.highestSatisfiedGrade(grades, Map.of(
				GradeMetricType.STORY_COUNT, 35L,
				GradeMetricType.FOLLOWER_COUNT, 1_500L,
				GradeMetricType.RECEIVED_LIKE_COUNT, 6_200L
		)).orElseThrow();

		assertThat(highest.gradeCode()).isEqualTo(GradeCode.EXPLORER);
	}

	@Test
	void staysAtRookieWhenNoPromotionThresholdIsMet() {
		Grade highest = evaluator.highestSatisfiedGrade(grades, Map.of()).orElseThrow();

		assertThat(highest.gradeCode()).isEqualTo(GradeCode.ROOKIE);
	}

	@Test
	void doesNotSelectHigherGradeWhenAnyMetricIsBelowThreshold() {
		Grade highest = evaluator.highestSatisfiedGrade(grades, Map.of(
				GradeMetricType.STORY_COUNT, 35L,
				GradeMetricType.FOLLOWER_COUNT, 1_500L,
				GradeMetricType.RECEIVED_LIKE_COUNT, 100L
		)).orElseThrow();

		assertThat(highest.gradeCode()).isEqualTo(GradeCode.LEAF);
	}

	@Test
	void returnsPromotionOnlyWhenTargetLevelIsHigher() {
		Grade rookie = grade(GradeCode.ROOKIE);
		Grade explorer = grade(GradeCode.EXPLORER);

		assertThat(evaluator.promotionTarget(rookie, explorer)).contains(explorer);
		assertThat(evaluator.promotionTarget(explorer, explorer)).isEmpty();
		assertThat(evaluator.promotionTarget(explorer, rookie)).isEmpty();
	}

	private Grade grade(GradeCode gradeCode) {
		return grades.stream()
				.filter(candidate -> candidate.gradeCode() == gradeCode)
				.findFirst()
				.orElseThrow();
	}
}
