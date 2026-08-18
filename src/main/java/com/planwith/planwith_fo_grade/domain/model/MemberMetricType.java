package com.planwith.planwith_fo_grade.domain.model;

/**
 * 회원 Metric({@code member_grade_metric})에서 사용하는 Metric 타입.
 * {@link GradeMetricType}에 {@code POST_COUNT}가 추가된 superset이다.
 */
public enum MemberMetricType {
	STORY_COUNT,
	POST_COUNT,
	FOLLOWER_COUNT,
	RECEIVED_LIKE_COUNT;

	public static MemberMetricType fromGradeMetricType(GradeMetricType gradeMetricType) {
		return switch (gradeMetricType) {
			case STORY_COUNT -> STORY_COUNT;
			case FOLLOWER_COUNT -> FOLLOWER_COUNT;
			case RECEIVED_LIKE_COUNT -> RECEIVED_LIKE_COUNT;
		};
	}
}
