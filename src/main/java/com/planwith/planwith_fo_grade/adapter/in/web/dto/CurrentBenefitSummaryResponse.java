package com.planwith.planwith_fo_grade.adapter.in.web.dto;

public record CurrentBenefitSummaryResponse(
		String gradeCode,
		String gradeName,
		int gradeLevel,
		int monthlyTokenAmount,
		boolean profileBadge,
		boolean profileSpecialBorder,
		boolean membershipPublicStory,
		boolean membershipAccess,
		String storyPriorityExposure
) {
}
