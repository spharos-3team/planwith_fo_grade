package com.planwith.planwith_fo_grade.application.command;

public record GrantGradeRewardCommand(
		String memberUuid,
		String gradeCode,
		String rewardType
) {
}
