package com.planwith.planwith_fo_grade.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class GradeRewardHistoryTest {

	private final MemberUuid memberUuid = MemberUuid.from(UUID.randomUUID().toString());
	private final LocalDateTime createdAt = LocalDateTime.of(2026, 8, 18, 0, 0);

	@Test
	void createsCompletedRewardHistory() {
		GradeRewardHistory history = GradeRewardHistory.create(
				memberUuid, 3L, "2026-08", 100L, createdAt
		);

		assertThat(history.rewardId()).isNull();
		assertThat(history.rewardMonth()).isEqualTo("2026-08");
		assertThat(history.tokenAmount()).isEqualTo(100L);
		assertThat(history.rewardStatus()).isEqualTo(RewardStatus.COMPLETED);
	}

	@Test
	void completesReadyReward() {
		GradeRewardHistory ready = GradeRewardHistory.createReady(
				memberUuid, 3L, "2026-08", 100L, createdAt
		);

		GradeRewardHistory completed = ready.complete();

		assertThat(completed.rewardStatus()).isEqualTo(RewardStatus.COMPLETED);
	}

	@Test
	void rejectsInvalidRewardMonthFormat() {
		assertThatThrownBy(() -> GradeRewardHistory.create(
				memberUuid, 3L, "202608", 100L, createdAt
		)).isInstanceOf(InvalidGradeException.class);
	}

	@Test
	void rejectsCancelingCompletedReward() {
		GradeRewardHistory completed = GradeRewardHistory.create(
				memberUuid, 3L, "2026-08", 100L, createdAt
		);

		assertThatThrownBy(completed::cancel).isInstanceOf(InvalidGradeException.class);
	}
}
