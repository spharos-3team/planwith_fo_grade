package com.planwith.planwith_fo_grade.application.event;

import java.util.Objects;

import com.planwith.planwith_fo_grade.domain.service.GradeBenefitSummary;

/**
 * 회원 등급이 실제로 변경되었을 때 Member / Story / Membership 서비스가 구독하는 계약.
 * Grade 서비스는 배지·테두리·스토리 노출·멤버십 기능을 직접 실행하지 않고,
 * 변경된 등급과 혜택 사용 가능 여부만 전달한다.
 * Kafka 토픽: {@code planwith.grade.changed}
 */
public record GradeChangedEvent(
		String eventUuid,
		String memberUuid,
		String previousGradeCode,
		String currentGradeCode,
		int previousGradeLevel,
		int currentGradeLevel,
		String changedAt,
		CurrentBenefits currentBenefits
) {
	public static final String EVENT_TYPE = "GradeChanged";

	public record CurrentBenefits(
			int monthlyTokenAmount,
			boolean profileBadge,
			boolean profileSpecialBorder,
			boolean membershipPublicStory,
			boolean membershipAccess,
			String storyPriorityExposure
	) {
		public static CurrentBenefits from(GradeBenefitSummary summary) {
			Objects.requireNonNull(summary, "Benefit summary is required.");
			return new CurrentBenefits(
					summary.monthlyTokenAmount(),
					summary.profileBadge(),
					summary.profileSpecialBorder(),
					summary.membershipPublicStory(),
					summary.membershipAccess(),
					summary.storyPriorityExposure()
			);
		}
	}

	public static GradeChangedEvent of(
			String eventUuid,
			String memberUuid,
			String previousGradeCode,
			String currentGradeCode,
			int previousGradeLevel,
			int currentGradeLevel,
			String changedAt,
			CurrentBenefits currentBenefits
	) {
		return new GradeChangedEvent(
				eventUuid,
				memberUuid,
				previousGradeCode,
				currentGradeCode,
				previousGradeLevel,
				currentGradeLevel,
				changedAt,
				Objects.requireNonNull(currentBenefits, "Current benefits are required.")
		);
	}
}
