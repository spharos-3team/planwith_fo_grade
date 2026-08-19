package com.planwith.planwith_fo_grade.application.event;

/**
 * 월간 토큰 보상이 확정되었을 때 Token Service가 구독하는 계약.
 * Kafka 토픽: {@code planwith.grade.reward-granted}
 */
public record GradeRewardGrantedEvent(
		String eventUuid,
		String memberUuid,
		String gradeCode,
		int gradeLevel,
		String rewardMonth,
		long tokenAmount,
		String rewardType,
		String grantedAt
) {
	public static final String EVENT_TYPE = "GradeRewardGranted";
	public static final String REWARD_TYPE_MONTHLY_FREE_TOKEN = "MONTHLY_FREE_TOKEN";

	public static GradeRewardGrantedEvent of(
			String eventUuid,
			String memberUuid,
			String gradeCode,
			int gradeLevel,
			String rewardMonth,
			long tokenAmount,
			String grantedAt
	) {
		return new GradeRewardGrantedEvent(
				eventUuid,
				memberUuid,
				gradeCode,
				gradeLevel,
				rewardMonth,
				tokenAmount,
				REWARD_TYPE_MONTHLY_FREE_TOKEN,
				grantedAt
		);
	}
}
