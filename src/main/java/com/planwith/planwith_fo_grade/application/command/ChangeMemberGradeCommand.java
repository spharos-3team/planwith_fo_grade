package com.planwith.planwith_fo_grade.application.command;

public record ChangeMemberGradeCommand(
		String memberUuid,
		String fromGradeCode,
		String toGradeCode
) {
}
