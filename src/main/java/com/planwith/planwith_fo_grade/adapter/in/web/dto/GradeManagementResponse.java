package com.planwith.planwith_fo_grade.adapter.in.web.dto;

import java.util.List;

public record GradeManagementResponse(
		CurrentGradeResponse currentGrade,
		CurrentMetricsResponse currentMetrics,
		NextGradeResponse nextGrade,
		ProgressResponse progress,
		CurrentBenefitSummaryResponse currentBenefits
) {

	public record CurrentGradeResponse(
			String code,
			String name,
			int level,
			List<GradeBenefitResponse> benefits
	) {
	}

	public record CurrentMetricsResponse(
			long storyCount,
			long followerCount,
			long receivedLikeCount
	) {
	}

	public record NextGradeResponse(
			String code,
			String name,
			List<GradeConditionResponse> conditions
	) {
	}

	public record ProgressResponse(
			MetricProgressResponse story,
			MetricProgressResponse follower,
			MetricProgressResponse receivedLike
	) {
	}

	public record MetricProgressResponse(
			long current,
			long required,
			long remaining,
			int percentage
	) {
	}
}
