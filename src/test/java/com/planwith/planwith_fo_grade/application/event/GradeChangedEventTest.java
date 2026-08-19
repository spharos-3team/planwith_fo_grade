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
				4,
				"2026-08-19T06:00:00Z"
		);

		assertThat(event.eventUuid()).isEqualTo("event-uuid");
		assertThat(event.memberUuid()).isEqualTo("member-uuid");
		assertThat(event.previousGrade()).isEqualTo("TRAVELER");
		assertThat(event.currentGrade()).isEqualTo("EXPLORER");
		assertThat(event.gradeLevel()).isEqualTo(4);
		assertThat(event.changedAt()).isEqualTo("2026-08-19T06:00:00Z");
	}
}
