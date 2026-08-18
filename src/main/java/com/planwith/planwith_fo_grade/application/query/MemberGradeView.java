package com.planwith.planwith_fo_grade.application.query;

public record MemberGradeView(
		String memberUuid,
		String currentGradeCode,
		String currentBenefitSummary,
		String nextGradeCode,
		String nextGradeCondition,
		int achievementRate
) {
}
