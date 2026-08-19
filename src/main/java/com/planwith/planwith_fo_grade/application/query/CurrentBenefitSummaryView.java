package com.planwith.planwith_fo_grade.application.query;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.service.GradeBenefitSummary;

public record CurrentBenefitSummaryView(
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

	public static CurrentBenefitSummaryView from(Grade grade) {
		GradeBenefitSummary summary = GradeBenefitSummary.from(grade.benefits());
		return new CurrentBenefitSummaryView(
				grade.gradeCode().name(),
				grade.gradeName(),
				grade.gradeLevel(),
				summary.monthlyTokenAmount(),
				summary.profileBadge(),
				summary.profileSpecialBorder(),
				summary.membershipPublicStory(),
				summary.membershipAccess(),
				summary.storyPriorityExposure()
		);
	}
}
