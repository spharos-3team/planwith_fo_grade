package com.planwith.planwith_fo_grade.application.port.out;

public record GradeOutboxMessage(
		String eventUuid,
		String aggregateType,
		String aggregateUuid,
		String eventType,
		String payload
) {
}
