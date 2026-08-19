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

		RecordGradeMetricCommand command = RecordGradeMetricCommandFactory.from(
				MetricEventType.LIKE_CREATED,
				memberUuid
		);

		assertThat(command.memberUuid()).isEqualTo(memberUuid);
		assertThat(command.metricType()).isEqualTo(MemberMetricType.RECEIVED_LIKE_COUNT.name());
		assertThat(command.delta()).isEqualTo(1L);
	}

	@Test
	void rejectsBlankOwnerUuid() {
		assertThatThrownBy(() -> RecordGradeMetricCommandFactory.from(MetricEventType.STORY_CREATED, " "))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
