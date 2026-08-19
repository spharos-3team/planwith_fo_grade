package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;
import com.planwith.planwith_fo_grade.application.command.GrantMonthlyGradeRewardsCommand;
import com.planwith.planwith_fo_grade.application.port.in.GrantGradeRewardUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class GrantMonthlyGradeRewardsServiceTest {

	private final LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);

	@Test
	void grantsOnlyActiveMembersForTargetMonth() {
		String activeUuid = UUID.randomUUID().toString();
		String suspendedUuid = UUID.randomUUID().toString();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(4L, MemberUuid.from(activeUuid), assignedAt));
		memberPort.save(GradeMember.assign(2L, MemberUuid.from(suspendedUuid), assignedAt).suspend());
		RecordingGrantGradeRewardUseCase grantUseCase = new RecordingGrantGradeRewardUseCase();
		GrantMonthlyGradeRewardsService service = new GrantMonthlyGradeRewardsService(memberPort, grantUseCase);

		service.grantAll(new GrantMonthlyGradeRewardsCommand("2026-08"));

		assertThat(grantUseCase.commands).hasSize(1);
		assertThat(grantUseCase.commands.get(0).memberUuid()).isEqualTo(activeUuid);
		assertThat(grantUseCase.commands.get(0).rewardMonth()).isEqualTo("2026-08");
		assertThat(grantUseCase.commands.get(0).rewardType()).isEqualTo("MONTHLY_FREE_TOKEN");
	}

	@Test
	void continuesWhenOneMemberGrantFails() {
		String firstUuid = UUID.randomUUID().toString();
		String secondUuid = UUID.randomUUID().toString();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(1L, MemberUuid.from(firstUuid), assignedAt));
		memberPort.save(GradeMember.assign(2L, MemberUuid.from(secondUuid), assignedAt));
		List<String> requested = new ArrayList<>();
		GrantGradeRewardUseCase grantUseCase = command -> {
			requested.add(command.memberUuid());
			if (firstUuid.equals(command.memberUuid())) {
				throw new IllegalStateException("지급 실패");
			}
		};
		GrantMonthlyGradeRewardsService service = new GrantMonthlyGradeRewardsService(memberPort, grantUseCase);

		service.grantAll(new GrantMonthlyGradeRewardsCommand("2026-08"));

		assertThat(requested).containsExactlyInAnyOrder(firstUuid, secondUuid);
	}

	private static final class RecordingGrantGradeRewardUseCase implements GrantGradeRewardUseCase {

		private final List<GrantGradeRewardCommand> commands = new ArrayList<>();

		@Override
		public void grant(GrantGradeRewardCommand command) {
			commands.add(command);
		}
	}

	private static final class InMemoryGradeMemberPort implements GradeMemberPort {

		private final Map<UUID, GradeMember> members = new LinkedHashMap<>();

		@Override
		public Optional<GradeMember> findByMemberUuid(MemberUuid memberUuid) {
			return Optional.ofNullable(members.get(memberUuid.value()));
		}

		@Override
		public List<GradeMember> findAllActive() {
			return members.values().stream().filter(GradeMember::isActive).toList();
		}

		@Override
		public GradeMember save(GradeMember member) {
			members.put(member.memberUuid().value(), member);
			return member;
		}
	}
}
