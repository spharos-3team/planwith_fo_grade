package com.planwith.planwith_fo_grade.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class GradeMemberTest {

	private final MemberUuid memberUuid = MemberUuid.from(UUID.randomUUID().toString());
	private final LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 18, 10, 0);

	@Test
	void assignsInitialActiveGrade() {
		GradeMember member = GradeMember.assign(1L, memberUuid, assignedAt);

		assertThat(member.gradeId()).isEqualTo(1L);
		assertThat(member.memberUuid()).isEqualTo(memberUuid);
		assertThat(member.gradeStatus()).isEqualTo(GradeStatus.ACTIVE);
		assertThat(member.gradeAssignedAt()).isEqualTo(assignedAt);
		assertThat(member.lastEvaluatedAt()).isNull();
		assertThat(member.isActive()).isTrue();
	}

	@Test
	void changesGradeForActiveMember() {
		LocalDateTime changedAt = assignedAt.plusDays(1);
		GradeMember changed = GradeMember.assign(1L, memberUuid, assignedAt)
				.changeGrade(2L, changedAt);

		assertThat(changed.gradeId()).isEqualTo(2L);
		assertThat(changed.gradeAssignedAt()).isEqualTo(changedAt);
		assertThat(changed.lastEvaluatedAt()).isEqualTo(changedAt);
	}

	@Test
	void rejectsGradeChangeForSuspendedMember() {
		GradeMember suspended = GradeMember.assign(1L, memberUuid, assignedAt).suspend();

		assertThatThrownBy(() -> suspended.changeGrade(2L, assignedAt.plusDays(1)))
				.isInstanceOf(InvalidGradeException.class);
	}
}
