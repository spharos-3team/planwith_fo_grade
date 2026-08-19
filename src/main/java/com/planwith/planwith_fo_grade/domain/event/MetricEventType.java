package com.planwith.planwith_fo_grade.domain.event;

import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;

/**
 * 외부 서비스에서 수신하는 Metric 입력 이벤트.
 * 등급 계산 대상 Metric과 증감 방향을 이벤트 타입별로 고정한다.
 */
public enum MetricEventType {
	STORY_CREATED(MemberMetricType.STORY_COUNT, 1L),
	STORY_DELETED(MemberMetricType.STORY_COUNT, -1L),
	FOLLOW_CREATED(MemberMetricType.FOLLOWER_COUNT, 1L),
	FOLLOW_REMOVED(MemberMetricType.FOLLOWER_COUNT, -1L),
	LIKE_CREATED(MemberMetricType.RECEIVED_LIKE_COUNT, 1L),
	LIKE_REMOVED(MemberMetricType.RECEIVED_LIKE_COUNT, -1L);

	private final MemberMetricType metricType;
	private final long delta;

	MetricEventType(MemberMetricType metricType, long delta) {
		this.metricType = metricType;
		this.delta = delta;
	}

	public MemberMetricType metricType() {
		return metricType;
	}

	public long delta() {
		return delta;
	}
}
