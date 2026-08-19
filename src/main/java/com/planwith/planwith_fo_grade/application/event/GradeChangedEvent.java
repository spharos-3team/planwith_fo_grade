package com.planwith.planwith_fo_grade.application.event;

/**
 * 회원 등급이 실제로 변경되었을 때 Member / Story / Membership 서비스가 구독하는 계약.
 * Kafka 토픽: {@code planwith.grade.changed}
 */
public record GradeChangedEvent(
		String eventUuid,
		String memberUuid,
		String previousGradeCode,
		String currentGradeCode,
		int previousGradeLevel,
		int currentGradeLevel,
		String changedAt
) {
	public static final String EVENT_TYPE = "GradeChanged";

	public static GradeChangedEvent of(
			String eventUuid,
			String memberUuid,
			String previousGradeCode,
			String currentGradeCode,
			int previousGradeLevel,
			int currentGradeLevel,
			String changedAt
	) {
		return new GradeChangedEvent(
				eventUuid,
				memberUuid,
				previousGradeCode,
				currentGradeCode,
				previousGradeLevel,
				currentGradeLevel,
				changedAt
		);
	}
}
