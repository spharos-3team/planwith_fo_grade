package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.application.command.AssignInitialGradeCommand;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeStatus;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class AssignInitialGradeServiceTest {

	private final LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
	private final String memberUuid = UUID.randomUUID().toString();

	@Test
	void assignsLowestGradeToNewMember() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withRookieAndLeaf();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		AssignInitialGradeService service = new AssignInitialGradeService(criteriaPort, memberPort);

		service.assign(new AssignInitialGradeCommand(memberUuid, assignedAt));

		GradeMember saved = memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(1L);
		assertThat(saved.gradeStatus()).isEqualTo(GradeStatus.ACTIVE);
		assertThat(saved.gradeAssignedAt()).isEqualTo(assignedAt);
		assertThat(criteriaPort.findLowestGrade().orElseThrow().gradeCode()).isEqualTo(GradeCode.ROOKIE);
	}

	@Test
	void skipsWhenMemberAlreadyHasGrade() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withRookieAndLeaf();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		AssignInitialGradeService service = new AssignInitialGradeService(criteriaPort, memberPort);
		AssignInitialGradeCommand command = new AssignInitialGradeCommand(memberUuid, assignedAt);

		service.assign(command);
		service.assign(new AssignInitialGradeCommand(memberUuid, assignedAt.plusDays(1)));

		GradeMember saved = memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(memberPort.saveCount).isEqualTo(1);
		assertThat(saved.gradeAssignedAt()).isEqualTo(assignedAt);
	}

	@Test
	void failsWhenLowestGradeCriteriaIsMissing() {
		AssignInitialGradeService service = new AssignInitialGradeService(
				new InMemoryGradeCriteriaPort(),
				new InMemoryGradeMemberPort()
		);

		assertThatThrownBy(() -> service.assign(new AssignInitialGradeCommand(memberUuid, assignedAt)))
				.isInstanceOf(InvalidGradeException.class);
	}

	private static final class InMemoryGradeCriteriaPort implements GradeCriteriaPort {

		private final Map<GradeCode, Grade> grades = new LinkedHashMap<>();

		private static InMemoryGradeCriteriaPort withRookieAndLeaf() {
			InMemoryGradeCriteriaPort port = new InMemoryGradeCriteriaPort();
			port.grades.put(GradeCode.ROOKIE, Grade.reconstitute(
					1L, GradeCode.ROOKIE, "새싹", 1, null, List.of(), List.of()
			));
			port.grades.put(GradeCode.LEAF, Grade.reconstitute(
					2L, GradeCode.LEAF, "잎새", 2, null, List.of(), List.of()
			));
			return port;
		}

		@Override
		public List<Grade> findAll() {
			return new ArrayList<>(grades.values());
		}

		@Override
		public Optional<Grade> findByGradeCode(GradeCode gradeCode) {
			return Optional.ofNullable(grades.get(gradeCode));
		}

		@Override
		public Optional<Grade> findLowestGrade() {
			return grades.values().stream()
					.min(Comparator.comparingInt(Grade::gradeLevel));
		}

		@Override
		public Grade save(Grade grade) {
			grades.put(grade.gradeCode(), grade);
			return grade;
		}
	}

	private static final class InMemoryGradeMemberPort implements GradeMemberPort {

		private final Map<UUID, GradeMember> members = new LinkedHashMap<>();
		private int saveCount;

		@Override
		public Optional<GradeMember> findByMemberUuid(MemberUuid memberUuid) {
			return Optional.ofNullable(members.get(memberUuid.value()));
		}

		@Override
		public GradeMember save(GradeMember member) {
			saveCount++;
			members.put(member.memberUuid().value(), member);
			return member;
		}
	}
}
