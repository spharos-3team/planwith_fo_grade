package com.planwith.planwith_fo_grade.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.service.GradeBenefitSummary;

class GradeChangedEventTest {

	@Test
	void definesContractForDownstreamServicesWithoutExecutingBenefits() {
		GradeChangedEvent.CurrentBenefits currentBenefits = GradeChangedEvent.CurrentBenefits.from(
				GradeBenefitSummary.from(GradeCriteriaCatalog.initialGrades().stream()
						.filter(grade -> grade.gradeCode() == GradeCode.EXPLORER)
						.findFirst()
						.orElseThrow()
						.benefits())
		);
		GradeChangedEvent event = GradeChangedEvent.of(
				"event-uuid",
				"member-uuid",
				"TRAVELER",
				"EXPLORER",
				3,
				4,
				"2026-08-19T06:00:00Z",
				currentBenefits
		);

		assertThat(event.eventUuid()).isEqualTo("event-uuid");
		assertThat(event.memberUuid()).isEqualTo("member-uuid");
		assertThat(event.previousGradeCode()).isEqualTo("TRAVELER");
		assertThat(event.currentGradeCode()).isEqualTo("EXPLORER");
		assertThat(event.previousGradeLevel()).isEqualTo(3);
		assertThat(event.currentGradeLevel()).isEqualTo(4);
		assertThat(event.changedAt()).isEqualTo("2026-08-19T06:00:00Z");
		assertThat(event.currentBenefits().monthlyTokenAmount()).isEqualTo(50);
		assertThat(event.currentBenefits().profileBadge()).isTrue();
		assertThat(event.currentBenefits().profileSpecialBorder()).isFalse();
		assertThat(event.currentBenefits().membershipPublicStory()).isTrue();
		assertThat(event.currentBenefits().membershipAccess()).isFalse();
		assertThat(event.currentBenefits().storyPriorityExposure()).isNull();
		assertThat(GradeChangedEvent.EVENT_TYPE).isEqualTo("GradeChanged");
	}
}
