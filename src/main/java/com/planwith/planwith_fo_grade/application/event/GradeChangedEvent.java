package com.planwith.planwith_fo_grade.application.event;

public record GradeChangedEvent(
		String eventUuid,
		String eventType,
		String memberUuid,
		String fromGradeCode,
		String toGradeCode,
		String occurredAt
) {
	public static final String EVENT_TYPE = "GradeChanged";

	public static GradeChangedEvent of(
			String eventUuid,
			String memberUuid,
			String fromGradeCode,
			String toGradeCode,
			String occurredAt
	) {
		return new GradeChangedEvent(
				eventUuid,
				EVENT_TYPE,
				memberUuid,
				fromGradeCode,
				toGradeCode,
				occurredAt
		);
	}
}
