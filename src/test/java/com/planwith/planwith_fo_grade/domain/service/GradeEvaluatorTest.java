package com.planwith.planwith_fo_grade.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;

class GradeEvaluatorTest {

	private final GradeEvaluator evaluator = new GradeEvaluator();
	private final List<Grade> grades = GradeCriteriaCatalog.initialGrades();

	@ParameterizedTest(name = "story={0}, follower={1}, like={2} → {3}")
	@CsvSource({
			"9, 100, 500, LEAF",
			"10, 99, 500, LEAF",
			"10, 100, 499, LEAF",
			"10, 100, 500, TRAVELER"
	})
	void requiresEveryTravelerConditionToPromoteFromLeaf(
			long storyCount,
			long followerCount,
			long receivedLikeCount,
			GradeCode expectedGrade
	) {
		Grade highest = evaluator.highestSatisfiedGrade(grades, Map.of(
				GradeMetricType.STORY_COUNT, storyCount,
				GradeMetricType.FOLLOWER_COUNT, followerCount,
				GradeMetricType.RECEIVED_LIKE_COUNT, receivedLikeCount
		)).orElseThrow();

		assertThat(highest.gradeCode()).isEqualTo(expectedGrade);
	}

	@Test
	void selectsAdventureWhenMetricsSatisfyMultipleGrades() {
		Grade highest = evaluator.highestSatisfiedGrade(grades, Map.of(
				GradeMetricType.STORY_COUNT, 110L,
				GradeMetricType.FOLLOWER_COUNT, 15_000L,
				GradeMetricType.RECEIVED_LIKE_COUNT, 40_000L
		)).orElseThrow();

		assertThat(highest.gradeCode()).isEqualTo(GradeCode.ADVENTURE);
	}

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
