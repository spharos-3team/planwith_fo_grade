package com.planwith.planwith_fo_grade.domain.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;

class MetricEventTypeTest {

	@Test
	void mapsEachEventToMetricTypeAndDelta() {
		assertThat(MetricEventType.STORY_CREATED.metricType()).isEqualTo(MemberMetricType.STORY_COUNT);
		assertThat(MetricEventType.STORY_CREATED.delta()).isEqualTo(1L);
		assertThat(MetricEventType.STORY_DELETED.metricType()).isEqualTo(MemberMetricType.STORY_COUNT);
		assertThat(MetricEventType.STORY_DELETED.delta()).isEqualTo(-1L);

		assertThat(MetricEventType.FOLLOW_CREATED.metricType()).isEqualTo(MemberMetricType.FOLLOWER_COUNT);
		assertThat(MetricEventType.FOLLOW_CREATED.delta()).isEqualTo(1L);
		assertThat(MetricEventType.FOLLOW_REMOVED.metricType()).isEqualTo(MemberMetricType.FOLLOWER_COUNT);
		assertThat(MetricEventType.FOLLOW_REMOVED.delta()).isEqualTo(-1L);

		assertThat(MetricEventType.LIKE_CREATED.metricType()).isEqualTo(MemberMetricType.RECEIVED_LIKE_COUNT);
		assertThat(MetricEventType.LIKE_CREATED.delta()).isEqualTo(1L);
		assertThat(MetricEventType.LIKE_REMOVED.metricType()).isEqualTo(MemberMetricType.RECEIVED_LIKE_COUNT);
		assertThat(MetricEventType.LIKE_REMOVED.delta()).isEqualTo(-1L);
	}
}
