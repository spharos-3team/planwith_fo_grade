package com.planwith.planwith_fo_grade.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;

class GradeBenefitSummaryTest {

	@Test
	void summarizesRookieAsTokenOnly() {
		GradeBenefitSummary summary = GradeBenefitSummary.from(grade(GradeCode.ROOKIE).benefits());

		assertThat(summary.monthlyTokenAmount()).isEqualTo(10);
		assertThat(summary.profileBadge()).isFalse();
		assertThat(summary.profileSpecialBorder()).isFalse();
		assertThat(summary.membershipPublicStory()).isFalse();
		assertThat(summary.membershipAccess()).isFalse();
		assertThat(summary.storyPriorityExposure()).isNull();
	}

	@Test
	void summarizesExplorerBadgeAndMembershipPublicStory() {
		GradeBenefitSummary summary = GradeBenefitSummary.from(grade(GradeCode.EXPLORER).benefits());

		assertThat(summary.monthlyTokenAmount()).isEqualTo(50);
		assertThat(summary.profileBadge()).isTrue();
		assertThat(summary.profileSpecialBorder()).isFalse();
		assertThat(summary.membershipPublicStory()).isTrue();
		assertThat(summary.membershipAccess()).isFalse();
		assertThat(summary.storyPriorityExposure()).isNull();
	}

	@Test
	void summarizesAdventureMembershipAndStoryPriority() {
		GradeBenefitSummary summary = GradeBenefitSummary.from(grade(GradeCode.ADVENTURE).benefits());

		assertThat(summary.monthlyTokenAmount()).isEqualTo(70);
		assertThat(summary.profileBadge()).isTrue();
		assertThat(summary.profileSpecialBorder()).isTrue();
		assertThat(summary.membershipPublicStory()).isTrue();
		assertThat(summary.membershipAccess()).isTrue();
		assertThat(summary.storyPriorityExposure()).isEqualTo(GradeBenefitSummary.STORY_PRIORITY_ADVENTURE);
	}

	@Test
	void summarizesPlanwithAsHighestStoryPriority() {
		GradeBenefitSummary summary = GradeBenefitSummary.from(grade(GradeCode.PLANWITH).benefits());

		assertThat(summary.monthlyTokenAmount()).isEqualTo(120);
		assertThat(summary.membershipAccess()).isTrue();
		assertThat(summary.storyPriorityExposure()).isEqualTo(GradeBenefitSummary.STORY_PRIORITY_HIGHEST);
	}

	private static Grade grade(GradeCode gradeCode) {
		return GradeCriteriaCatalog.initialGrades().stream()
				.filter(candidate -> candidate.gradeCode() == gradeCode)
				.findFirst()
				.orElseThrow();
	}
}
