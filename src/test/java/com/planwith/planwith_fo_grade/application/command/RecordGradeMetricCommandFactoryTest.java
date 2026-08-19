package com.planwith.planwith_fo_grade.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.event.MetricEventType;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;

class RecordGradeMetricCommandFactoryTest {

	@Test
	void createsCommandFromEventTypeAndOwnerUuid() {
		String memberUuid = UUID.randomUUID().toString();
		String eventUuid = UUID.randomUUID().toString();

		RecordGradeMetricCommand command = RecordGradeMetricCommandFactory.from(
				MetricEventType.LIKE_CREATED,
				memberUuid,
				eventUuid,
				16L
		);

		assertThat(command.memberUuid()).isEqualTo(memberUuid);
		assertThat(command.eventUuid()).isEqualTo(eventUuid);
		assertThat(command.metricType()).isEqualTo(MemberMetricType.RECEIVED_LIKE_COUNT.name());
		assertThat(command.delta()).isEqualTo(1L);
		assertThat(command.sourceVersion()).isEqualTo(16L);
	}

	@Test
	void rejectsBlankOwnerUuid() {
		assertThatThrownBy(() -> RecordGradeMetricCommandFactory.from(
				MetricEventType.STORY_CREATED,
				" ",
				UUID.randomUUID().toString()
		)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejectsBlankEventUuid() {
		assertThatThrownBy(() -> RecordGradeMetricCommandFactory.from(
				MetricEventType.STORY_CREATED,
				UUID.randomUUID().toString(),
				" "
		)).isInstanceOf(IllegalArgumentException.class);
	}
}
