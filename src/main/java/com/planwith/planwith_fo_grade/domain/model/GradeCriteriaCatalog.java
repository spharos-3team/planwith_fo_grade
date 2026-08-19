package com.planwith.planwith_fo_grade.domain.model;

import java.util.List;

/**
 * 등급 기준 초기 정책 정의.
 * 평가 시에는 이 클래스의 GradeCode 분기가 아니라 DB에 적재된
 * {@code grade_condition.threshold_value}를 사용한다.
 */
public final class GradeCriteriaCatalog {

	private static final Long UNSAVED_GRADE_ID = 0L;
	private static final String MONTHLY_TOKEN_BENEFIT_NAME = "월간 무료 토큰";

	private GradeCriteriaCatalog() {
	}

	public static List<Grade> initialGrades() {
		return List.of(
				createGrade(
						GradeCode.ROOKIE,
						"🌱 새싹",
						1,
						"가입 시 부여되는 기본 등급",
						List.of(),
						"10"
				),
				createGrade(
						GradeCode.LEAF,
						"🌿 잎새",
						2,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(3L, 10L, 30L),
						"20"
				),
				createGrade(
						GradeCode.TRAVELER,
						"✈️ 여행가",
						3,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(10L, 100L, 500L),
						"30"
				),
				createGrade(
						GradeCode.EXPLORER,
						"🧭 탐험가",
						4,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(30L, 1_000L, 5_000L),
						"50"
				),
				createGrade(
						GradeCode.ADVENTURE,
						"🌏 모험가",
						5,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(100L, 10_000L, 30_000L),
						"70"
				),
				createGrade(
						GradeCode.PLANWITH,
						"👑 PLAN&WITH 마스터",
						6,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(200L, 50_000L, 150_000L),
						"120"
				)
		);
	}

	private static Grade createGrade(
			GradeCode gradeCode,
			String gradeName,
			int gradeLevel,
			String description,
			List<GradeCondition> conditions,
			String monthlyTokenAmount
	) {
		return Grade.create(
				gradeCode,
				gradeName,
				gradeLevel,
				description,
				conditions,
				List.of(monthlyTokenBenefit(monthlyTokenAmount))
		);
	}

	private static List<GradeCondition> thresholds(
			long storyCount,
			long followerCount,
			long receivedLikeCount
	) {
		return List.of(
				condition(GradeMetricType.STORY_COUNT, "스토리", storyCount, 1),
				condition(GradeMetricType.FOLLOWER_COUNT, "팔로워", followerCount, 2),
				condition(GradeMetricType.RECEIVED_LIKE_COUNT, "받은 좋아요", receivedLikeCount, 3)
		);
	}

	private static GradeCondition condition(
			GradeMetricType metricType,
			String conditionName,
			long thresholdValue,
			int sortOrder
	) {
		return GradeCondition.create(
				UNSAVED_GRADE_ID,
				metricType,
				conditionName,
				thresholdValue,
				sortOrder,
				metricType.name() + " >= " + thresholdValue
		);
	}

	private static GradeBenefit monthlyTokenBenefit(String tokenAmount) {
		return GradeBenefit.create(
				UNSAVED_GRADE_ID,
				BenefitCode.MONTHLY_FREE_TOKEN,
				MONTHLY_TOKEN_BENEFIT_NAME,
				tokenAmount,
				"매월 지급되는 무료 토큰 수량",
				1
		);
	}
}
