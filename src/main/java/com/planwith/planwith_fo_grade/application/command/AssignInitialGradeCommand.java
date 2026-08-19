package com.planwith.planwith_fo_grade.application.command;

import java.time.LocalDateTime;

public record AssignInitialGradeCommand(
		String memberUuid,
		LocalDateTime assignedAt
) {
}
