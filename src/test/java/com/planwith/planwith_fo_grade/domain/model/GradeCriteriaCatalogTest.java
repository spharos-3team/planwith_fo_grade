package com.planwith.planwith_fo_grade.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class GradeCriteriaCatalogTest {

	@Test
	void definesSixGradesWithMonthlyTokenBenefits() {
		List<Grade> grades = GradeCriteriaCatalog.initialGrades();

		assertThat(grades).extracting(Grade::gradeCode).containsExactly(
				GradeCode.ROOKIE,
				GradeCode.LEAF,
				GradeCode.TRAVELER,
				GradeCode.EXPLORER,
				GradeCode.ADVENTURE,
				GradeCode.PLANWITH
		);
		assertThat(grades).extracting(Grade::gradeName).containsExactly(
				"🌱 새싹",
				"🧳 잎새",
				"✈️ 여행가",
				"🧭 탐험가",
				"🌏 모험가",
				"👑 PLAN&WITH 마스터"
		);
		assertThat(tokenAmount(grades, GradeCode.ROOKIE)).isEqualTo("10");
		assertThat(tokenAmount(grades, GradeCode.LEAF)).isEqualTo("20");
		assertThat(tokenAmount(grades, GradeCode.TRAVELER)).isEqualTo("30");
		assertThat(tokenAmount(grades, GradeCode.EXPLORER)).isEqualTo("50");
		assertThat(tokenAmount(grades, GradeCode.ADVENTURE)).isEqualTo("70");
		assertThat(tokenAmount(grades, GradeCode.PLANWITH)).isEqualTo("120");
	}

	@Test
	void assignsBenefitsByUpdatedPolicy() {
		assertThat(benefitCodes(GradeCode.ROOKIE)).containsExactly(BenefitCode.MONTHLY_FREE_TOKEN);
		assertThat(benefitCodes(GradeCode.LEAF)).containsExactly(BenefitCode.MONTHLY_FREE_TOKEN);
		assertThat(benefitCodes(GradeCode.TRAVELER)).containsExactly(BenefitCode.MONTHLY_FREE_TOKEN);
		assertThat(benefitCodes(GradeCode.EXPLORER)).containsExactly(
				BenefitCode.MONTHLY_FREE_TOKEN,
				BenefitCode.PROFILE_BADGE,
				BenefitCode.MEMBERSHIP_PUBLIC_STORY
		);
		assertThat(benefitCodes(GradeCode.ADVENTURE)).containsExactly(
				BenefitCode.MONTHLY_FREE_TOKEN,
				BenefitCode.PROFILE_BADGE,
				BenefitCode.PROFILE_SPECIAL_BORDER,
				BenefitCode.NON_MEMBER_STORY_PRIORITY
		);
		assertThat(benefitCodes(GradeCode.PLANWITH)).containsExactly(
				BenefitCode.MONTHLY_FREE_TOKEN,
				BenefitCode.PROFILE_BADGE,
				BenefitCode.PROFILE_SPECIAL_BORDER,
				BenefitCode.NON_MEMBER_STORY_PRIORITY
		);
		assertThat(benefitValue(GradeCode.ADVENTURE, BenefitCode.NON_MEMBER_STORY_PRIORITY)).isEqualTo("ADVENTURE");
		assertThat(benefitValue(GradeCode.PLANWITH, BenefitCode.NON_MEMBER_STORY_PRIORITY)).isEqualTo("PLANWITH");
	}

	@Test
	void rookieHasNoPromotionConditions() {
		Grade rookie = grade(GradeCode.ROOKIE);

		assertThat(rookie.conditions()).isEmpty();
		assertThat(rookie.satisfiesAllConditions(Map.of())).isTrue();
	}

	@Test
	void travelerRequiresAllMetricThresholds() {
		Grade traveler = grade(GradeCode.TRAVELER);

		assertThat(threshold(traveler, GradeMetricType.STORY_COUNT)).isEqualTo(10L);
		assertThat(threshold(traveler, GradeMetricType.FOLLOWER_COUNT)).isEqualTo(100L);
		assertThat(threshold(traveler, GradeMetricType.RECEIVED_LIKE_COUNT)).isEqualTo(500L);

		assertThat(traveler.satisfiesAllConditions(Map.of(
				GradeMetricType.STORY_COUNT, 10L,
				GradeMetricType.FOLLOWER_COUNT, 100L,
				GradeMetricType.RECEIVED_LIKE_COUNT, 500L
		))).isTrue();
		assertThat(traveler.satisfiesAllConditions(Map.of(
				GradeMetricType.STORY_COUNT, 10L,
				GradeMetricType.FOLLOWER_COUNT, 99L,
				GradeMetricType.RECEIVED_LIKE_COUNT, 500L
		))).isFalse();
	}

	@Test
	void higherGradesKeepAndThresholds() {
		assertThresholds(GradeCode.LEAF, 3L, 10L, 30L);
		assertThresholds(GradeCode.EXPLORER, 30L, 1_000L, 5_000L);
		assertThresholds(GradeCode.ADVENTURE, 100L, 10_000L, 30_000L);
		assertThresholds(GradeCode.PLANWITH, 200L, 50_000L, 150_000L);
	}

	private static void assertThresholds(
			GradeCode gradeCode,
			long storyCount,
			long followerCount,
			long receivedLikeCount
	) {
		Grade grade = grade(gradeCode);
		assertThat(threshold(grade, GradeMetricType.STORY_COUNT)).isEqualTo(storyCount);
		assertThat(threshold(grade, GradeMetricType.FOLLOWER_COUNT)).isEqualTo(followerCount);
		assertThat(threshold(grade, GradeMetricType.RECEIVED_LIKE_COUNT)).isEqualTo(receivedLikeCount);
	}

	private static Grade grade(GradeCode gradeCode) {
		return GradeCriteriaCatalog.initialGrades().stream()
				.filter(candidate -> candidate.gradeCode() == gradeCode)
				.findFirst()
				.orElseThrow();
	}

	private static List<BenefitCode> benefitCodes(GradeCode gradeCode) {
		return grade(gradeCode).benefits().stream()
				.map(GradeBenefit::benefitCode)
				.toList();
	}

	private static String benefitValue(GradeCode gradeCode, BenefitCode benefitCode) {
		return grade(gradeCode).benefits().stream()
				.filter(benefit -> benefit.benefitCode() == benefitCode)
				.map(GradeBenefit::benefitValue)
				.findFirst()
				.orElseThrow();
	}

	private static String tokenAmount(List<Grade> grades, GradeCode gradeCode) {
		return grades.stream()
				.filter(grade -> grade.gradeCode() == gradeCode)
				.findFirst()
				.map(grade -> grade.benefits().get(0).benefitValue())
				.orElseThrow();
	}

	private static long threshold(Grade grade, GradeMetricType metricType) {
		Map<GradeMetricType, GradeCondition> conditions = grade.conditions().stream()
				.collect(Collectors.toMap(GradeCondition::metricType, Function.identity()));
		return conditions.get(metricType).thresholdValue();
	}
}
