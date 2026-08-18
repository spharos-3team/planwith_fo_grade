package com.planwith.planwith_fo_grade.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class GradeTest {

	@Test
	void createsGradeWithConditionsAndBenefits() {
		Long gradeId = 1L;
		GradeCondition condition = GradeCondition.create(
				gradeId, GradeMetricType.STORY_COUNT, "스토리 10개", 10L, 1, null
		);
		GradeBenefit benefit = GradeBenefit.create(
				gradeId, BenefitCode.MONTHLY_FREE_TOKEN, "월간 무료 토큰", "100", null, 1
		);

		Grade grade = Grade.create(
				GradeCode.LEAF,
				"Leaf",
				2,
				"초보 등급",
				List.of(condition),
				List.of(benefit)
		);

		assertThat(grade.gradeId()).isNull();
		assertThat(grade.gradeCode()).isEqualTo(GradeCode.LEAF);
		assertThat(grade.gradeName()).isEqualTo("Leaf");
		assertThat(grade.gradeLevel()).isEqualTo(2);
		assertThat(grade.conditions()).containsExactly(condition);
		assertThat(grade.benefits()).containsExactly(benefit);
	}

	@Test
	void satisfiesAllConditionsWhenEveryMetricMeetsThreshold() {
		Grade grade = gradeWithConditions(
				GradeCondition.create(1L, GradeMetricType.STORY_COUNT, "스토리", 10L, 1, null),
				GradeCondition.create(1L, GradeMetricType.FOLLOWER_COUNT, "팔로워", 5L, 2, null)
		);

		assertThat(grade.satisfiesAllConditions(Map.of(
				GradeMetricType.STORY_COUNT, 10L,
				GradeMetricType.FOLLOWER_COUNT, 5L
		))).isTrue();
	}

	@Test
	void doesNotSatisfyConditionsWhenAnyMetricIsBelowThreshold() {
		Grade grade = gradeWithConditions(
				GradeCondition.create(1L, GradeMetricType.STORY_COUNT, "스토리", 10L, 1, null)
		);

		assertThat(grade.satisfiesAllConditions(Map.of(
				GradeMetricType.STORY_COUNT, 9L
		))).isFalse();
	}

	@Test
	void protectsConditionsFromExternalMutation() {
		Grade grade = gradeWithConditions(
				GradeCondition.create(1L, GradeMetricType.STORY_COUNT, "스토리", 10L, 1, null)
		);

		assertThatThrownBy(() -> grade.conditions().add(
				GradeCondition.create(1L, GradeMetricType.FOLLOWER_COUNT, "팔로워", 1L, 2, null)
		)).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void rejectsNonPositiveGradeLevel() {
		assertThatThrownBy(() -> Grade.create(
				GradeCode.ROOKIE, "Rookie", 0, null, List.of(), List.of()
		)).isInstanceOf(InvalidGradeException.class);
	}

	private Grade gradeWithConditions(GradeCondition... conditions) {
		return Grade.create(
				GradeCode.TRAVELER,
				"Traveler",
				3,
				null,
				List.of(conditions),
				List.of()
		);
	}
}
