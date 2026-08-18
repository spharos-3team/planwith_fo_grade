package com.planwith.planwith_fo_grade.domain.model;

/**
 * 등급 승급 조건({@code grade_condition})에서 사용하는 Metric 타입.
 * {@code POST_COUNT}는 등급 조건에 포함되지 않는다.
 */
public enum GradeMetricType {
	STORY_COUNT,
	FOLLOWER_COUNT,
	RECEIVED_LIKE_COUNT
}
