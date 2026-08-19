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

import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.query.CurrentBenefitSummaryView;
import com.planwith.planwith_fo_grade.domain.exception.GradeNotFoundException;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_grade.domain.service.GradeBenefitSummary;

class GetCurrentBenefitsServiceTest {

	private final String memberUuid = UUID.randomUUID().toString();
	private final LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);

	@Test
	void returnsExplorerBenefitEligibilityWithoutExecutingBenefits() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		GetCurrentBenefitsService service = new GetCurrentBenefitsService(
				assigned(criteriaPort, GradeCode.EXPLORER),
				criteriaPort
		);

		CurrentBenefitSummaryView view = service.get(memberUuid);

		assertThat(view.gradeCode()).isEqualTo("EXPLORER");
		assertThat(view.monthlyTokenAmount()).isEqualTo(50);
		assertThat(view.profileBadge()).isTrue();
		assertThat(view.profileSpecialBorder()).isFalse();
		assertThat(view.membershipPublicStory()).isTrue();
		assertThat(view.membershipAccess()).isFalse();
		assertThat(view.storyPriorityExposure()).isNull();
	}

	@Test
	void returnsAdventureMembershipAccessAndStoryPriority() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		GetCurrentBenefitsService service = new GetCurrentBenefitsService(
				assigned(criteriaPort, GradeCode.ADVENTURE),
				criteriaPort
		);

		CurrentBenefitSummaryView view = service.get(memberUuid);

		assertThat(view.monthlyTokenAmount()).isEqualTo(70);
		assertThat(view.membershipAccess()).isTrue();
		assertThat(view.membershipPublicStory()).isTrue();
		assertThat(view.storyPriorityExposure()).isEqualTo(GradeBenefitSummary.STORY_PRIORITY_ADVENTURE);
	}

	@Test
	void throwsWhenMemberGradeIsMissing() {
		GetCurrentBenefitsService service = new GetCurrentBenefitsService(
				new InMemoryGradeMemberPort(),
				InMemoryGradeCriteriaPort.withCatalog()
		);

		assertThatThrownBy(() -> service.get(memberUuid))
				.isInstanceOf(GradeNotFoundException.class);
	}

	private InMemoryGradeMemberPort assigned(InMemoryGradeCriteriaPort criteriaPort, GradeCode gradeCode) {
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(
				criteriaPort.findByGradeCode(gradeCode).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		return memberPort;
	}

	private static final class InMemoryGradeCriteriaPort implements GradeCriteriaPort {

		private final Map<GradeCode, Grade> grades = new LinkedHashMap<>();

		private static InMemoryGradeCriteriaPort withCatalog() {
			InMemoryGradeCriteriaPort port = new InMemoryGradeCriteriaPort();
			long gradeId = 1L;
			for (Grade grade : GradeCriteriaCatalog.initialGrades()) {
				port.grades.put(grade.gradeCode(), Grade.reconstitute(
						gradeId++,
						grade.gradeCode(),
						grade.gradeName(),
						grade.gradeLevel(),
						grade.description(),
						grade.conditions(),
						grade.benefits()
				));
			}
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

		@Override
		public Optional<GradeMember> findByMemberUuid(MemberUuid memberUuid) {
			return Optional.ofNullable(members.get(memberUuid.value()));
		}

		@Override
		public GradeMember save(GradeMember member) {
			members.put(member.memberUuid().value(), member);
			return member;
		}
	}
}
