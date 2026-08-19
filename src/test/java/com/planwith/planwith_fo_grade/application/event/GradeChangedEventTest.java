package com.planwith.planwith_fo_grade.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GradeChangedEventTest {

	@Test
	void definesContractForDownstreamServices() {
		GradeChangedEvent event = GradeChangedEvent.of(
				"event-uuid",
				"member-uuid",
				"TRAVELER",
				"EXPLORER",
				3,
				4,
				"2026-08-19T06:00:00Z"
		);

		assertThat(event.eventUuid()).isEqualTo("event-uuid");
		assertThat(event.memberUuid()).isEqualTo("member-uuid");
		assertThat(event.previousGradeCode()).isEqualTo("TRAVELER");
		assertThat(event.currentGradeCode()).isEqualTo("EXPLORER");
		assertThat(event.previousGradeLevel()).isEqualTo(3);
		assertThat(event.currentGradeLevel()).isEqualTo(4);
		assertThat(event.changedAt()).isEqualTo("2026-08-19T06:00:00Z");
		assertThat(GradeChangedEvent.EVENT_TYPE).isEqualTo("GradeChanged");
	}
}
