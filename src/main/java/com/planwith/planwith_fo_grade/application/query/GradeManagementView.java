package com.planwith.planwith_fo_grade.application.query;

import java.util.List;

public record GradeManagementView(
		CurrentGradeView currentGrade,
		CurrentMetricsView currentMetrics,
		NextGradeView nextGrade,
		ProgressView progress
) {

	public record CurrentGradeView(
			String code,
			String name,
			int level,
			List<GradeBenefitView> benefits
	) {
	}

	public record CurrentMetricsView(
			long storyCount,
			long followerCount,
			long receivedLikeCount
	) {
	}

	public record NextGradeView(
			String code,
			String name,
			List<GradeConditionView> conditions
	) {
	}

	public record ProgressView(
			MetricProgressView story,
			MetricProgressView follower,
			MetricProgressView receivedLike
	) {
	}

	public record MetricProgressView(
			long current,
			long required,
			long remaining,
			int percentage
	) {
	}
}
