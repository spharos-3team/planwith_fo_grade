package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
import com.planwith.planwith_fo_grade.application.port.in.GrantGradeRewardUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeRewardHistoryPort;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
class GrantGradeRewardOutboxRollbackIntegrationTest {

	@Autowired
	private GrantGradeRewardUseCase grantGradeRewardUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private GradeRewardHistoryPort gradeRewardHistoryPort;

	@Test
	void rollsBackRewardHistoryWhenOutboxInsertIsFollowedByFailure() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));

		assertThatThrownBy(() -> grantGradeRewardUseCase.grant(new GrantGradeRewardCommand(
				memberUuid,
				GradeCode.EXPLORER.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		))).isInstanceOf(IllegalStateException.class)
				.hasMessage("Outbox INSERT 이후 강제 실패");

		assertThat(gradeRewardHistoryPort.existsByMemberUuidAndRewardMonth(
				MemberUuid.from(memberUuid),
				"2026-08"
		)).isFalse();
	}

	@TestConfiguration
	static class FailingOutboxAfterInsertConfig {

		@Bean
		@Primary
		GradeEventOutboxPort failingOutboxAfterInsert(SpringDataGradeOutboxRepository repository) {
			GradeEventOutboxAdapter delegate = new GradeEventOutboxAdapter(repository);
			return message -> {
				delegate.save(message);
				throw new IllegalStateException("Outbox INSERT 이후 강제 실패");
			};
		}
	}
}
