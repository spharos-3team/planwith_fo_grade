package com.planwith.planwith_fo_grade.domain.service;

import java.util.List;
import java.util.Objects;

import com.planwith.planwith_fo_grade.domain.model.GradeBenefit;

/**
 * 등급 혜택 기준 요약.
 * Grade 서비스는 혜택을 실행하지 않고, 다른 서비스가 참고할 사용 가능 여부만 제공한다.
 */
public record GradeBenefitSummary(
		int monthlyTokenAmount,
		boolean profileBadge,
		boolean profileSpecialBorder,
		boolean membershipPublicStory,
		boolean membershipAccess,
		String storyPriorityExposure
) {

	public static final String STORY_PRIORITY_ADVENTURE = "ADVENTURE";
	public static final String STORY_PRIORITY_HIGHEST = "HIGHEST";

	public static GradeBenefitSummary from(List<GradeBenefit> benefits) {
		Objects.requireNonNull(benefits, "Benefits are required.");
		int monthlyTokenAmount = 0;
		boolean profileBadge = false;
		boolean profileSpecialBorder = false;
		boolean membershipPublicStory = false;
		boolean membershipAccess = false;
		String storyPriorityExposure = null;
		for (GradeBenefit benefit : benefits) {
			switch (benefit.benefitCode()) {
				case MONTHLY_FREE_TOKEN -> monthlyTokenAmount = parseTokenAmount(benefit.benefitValue());
				case PROFILE_BADGE -> profileBadge = true;
				case PROFILE_SPECIAL_BORDER -> profileSpecialBorder = true;
				case MEMBERSHIP_PUBLIC_STORY -> membershipPublicStory = true;
				case MEMBERSHIP_ACCESS -> membershipAccess = true;
				case NON_MEMBER_STORY_PRIORITY -> storyPriorityExposure = benefit.benefitValue();
			}
		}
		return new GradeBenefitSummary(
				monthlyTokenAmount,
				profileBadge,
				profileSpecialBorder,
				membershipPublicStory,
				membershipAccess,
				storyPriorityExposure
		);
	}

	private static int parseTokenAmount(String value) {
		if (value == null || value.isBlank()) {
			return 0;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			return 0;
		}
	}
}
