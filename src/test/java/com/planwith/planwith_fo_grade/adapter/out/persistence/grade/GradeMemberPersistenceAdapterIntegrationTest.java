package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.command.AssignInitialGradeCommand;
import com.planwith.planwith_fo_grade.application.port.in.AssignInitialGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeStatus;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradeMemberPersistenceAdapterIntegrationTest {

	@Autowired
	private AssignInitialGradeUseCase assignInitialGradeUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Test
	void assignsRookieGradeToNewMember() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);

		assignInitialGradeUseCase.assign(new AssignInitialGradeCommand(memberUuid, assignedAt));

		GradeMember saved = gradeMemberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(
				gradeCriteriaPort.findLowestGrade().orElseThrow().gradeId()
		);
		assertThat(gradeCriteriaPort.findLowestGrade().orElseThrow().gradeCode()).isEqualTo(GradeCode.ROOKIE);
		assertThat(saved.gradeStatus()).isEqualTo(GradeStatus.ACTIVE);
		assertThat(saved.gradeAssignedAt()).isEqualTo(assignedAt);
	}

	@Test
	void doesNotOverwriteExistingMemberGrade() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime firstAssignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);

		assignInitialGradeUseCase.assign(new AssignInitialGradeCommand(memberUuid, firstAssignedAt));
		assignInitialGradeUseCase.assign(new AssignInitialGradeCommand(memberUuid, firstAssignedAt.plusHours(1)));

		GradeMember saved = gradeMemberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeAssignedAt()).isEqualTo(firstAssignedAt);
	}
}
