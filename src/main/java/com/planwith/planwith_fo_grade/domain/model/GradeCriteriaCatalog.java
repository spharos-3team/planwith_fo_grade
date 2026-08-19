package com.planwith.planwith_fo_grade.domain.model;

import java.util.List;

import com.planwith.planwith_fo_grade.domain.service.GradeBenefitSummary;

/**
 * 등급 기준 초기 정책 정의.
 * 평가 시에는 이 클래스의 GradeCode 분기가 아니라 DB에 적재된
 * {@code grade_condition.threshold_value}를 사용한다.
 */
public final class GradeCriteriaCatalog {

	private static final Long UNSAVED_GRADE_ID = 0L;
	private static final String MONTHLY_TOKEN_BENEFIT_NAME = "월간 무료 토큰";
	private static final String PROFILE_BADGE_NAME = "프로필 배지";
	private static final String MEMBERSHIP_PUBLIC_STORY_NAME = "멤버십 회원공개 스토리 작성";
	private static final String MEMBERSHIP_ACCESS_NAME = "멤버십 기능 사용";
	private static final String PROFILE_SPECIAL_BORDER_NAME = "프로필 특별 테두리";
	private static final String NON_MEMBER_STORY_PRIORITY_NAME = "비회원 스토리 우선 노출";

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
						List.of(monthlyTokenBenefit("10", 1))
				),
				createGrade(
						GradeCode.LEAF,
						"🧳 잎새",
						2,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(3L, 10L, 30L),
						List.of(monthlyTokenBenefit("20", 1))
				),
				createGrade(
						GradeCode.TRAVELER,
						"✈️ 여행가",
						3,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(10L, 100L, 500L),
						List.of(monthlyTokenBenefit("30", 1))
				),
				createGrade(
						GradeCode.EXPLORER,
						"🧭 탐험가",
						4,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(30L, 1_000L, 5_000L),
						List.of(
								monthlyTokenBenefit("50", 1),
								profileBadge(2),
								membershipPublicStory(3)
						)
				),
				createGrade(
						GradeCode.ADVENTURE,
						"🌏 모험가",
						5,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(100L, 10_000L, 30_000L),
						List.of(
								monthlyTokenBenefit("70", 1),
								profileBadge(2),
								profileSpecialBorder(3),
								membershipPublicStory(4),
								membershipAccess(5),
								nonMemberStoryPriority(
										GradeBenefitSummary.STORY_PRIORITY_ADVENTURE,
										"비회원이 스토리 진입 시 스토리 우선 노출 (플랜 마스터와 분리)",
										6
								)
						)
				),
				createGrade(
						GradeCode.PLANWITH,
						"👑 PLAN&WITH 마스터",
						6,
						"스토리, 팔로워, 받은 좋아요 조건을 모두 충족해야 승급한다",
						thresholds(200L, 50_000L, 150_000L),
						List.of(
								monthlyTokenBenefit("120", 1),
								profileBadge(2),
								profileSpecialBorder(3),
								membershipPublicStory(4),
								membershipAccess(5),
								nonMemberStoryPriority(
										GradeBenefitSummary.STORY_PRIORITY_HIGHEST,
										"비회원이 스토리 진입 시 스토리 우선 노출 (모험가와 분리, 최상위)",
										6
								)
						)
				)
		);
	}

	private static Grade createGrade(
			GradeCode gradeCode,
			String gradeName,
			int gradeLevel,
			String description,
			List<GradeCondition> conditions,
			List<GradeBenefit> benefits
	) {
		return Grade.create(
				gradeCode,
				gradeName,
				gradeLevel,
				description,
				conditions,
				benefits
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

	private static GradeBenefit monthlyTokenBenefit(String tokenAmount, int sortOrder) {
		return GradeBenefit.create(
				UNSAVED_GRADE_ID,
				BenefitCode.MONTHLY_FREE_TOKEN,
				MONTHLY_TOKEN_BENEFIT_NAME,
				tokenAmount,
				"매월 지급되는 무료 토큰 수량",
				sortOrder
		);
	}

	private static GradeBenefit profileBadge(int sortOrder) {
		return GradeBenefit.create(
				UNSAVED_GRADE_ID,
				BenefitCode.PROFILE_BADGE,
				PROFILE_BADGE_NAME,
				"true",
				"프로필 배지를 지급한다",
				sortOrder
		);
	}

	private static GradeBenefit membershipPublicStory(int sortOrder) {
		return GradeBenefit.create(
				UNSAVED_GRADE_ID,
				BenefitCode.MEMBERSHIP_PUBLIC_STORY,
				MEMBERSHIP_PUBLIC_STORY_NAME,
				"true",
				"멤버십 회원공개 스토리 작성이 가능하다",
				sortOrder
		);
	}

	private static GradeBenefit membershipAccess(int sortOrder) {
		return GradeBenefit.create(
				UNSAVED_GRADE_ID,
				BenefitCode.MEMBERSHIP_ACCESS,
				MEMBERSHIP_ACCESS_NAME,
				"true",
				"멤버십 기능 사용 가능 등급이다. 가입자 목록, 금액 설정, 수익 신청은 Membership Service가 수행한다",
				sortOrder
		);
	}

	private static GradeBenefit profileSpecialBorder(int sortOrder) {
		return GradeBenefit.create(
				UNSAVED_GRADE_ID,
				BenefitCode.PROFILE_SPECIAL_BORDER,
				PROFILE_SPECIAL_BORDER_NAME,
				"true",
				"프로필에 특별 테두리를 적용한다",
				sortOrder
		);
	}

	private static GradeBenefit nonMemberStoryPriority(String exposureGroup, String description, int sortOrder) {
		return GradeBenefit.create(
				UNSAVED_GRADE_ID,
				BenefitCode.NON_MEMBER_STORY_PRIORITY,
				NON_MEMBER_STORY_PRIORITY_NAME,
				exposureGroup,
				description,
				sortOrder
		);
	}
}
