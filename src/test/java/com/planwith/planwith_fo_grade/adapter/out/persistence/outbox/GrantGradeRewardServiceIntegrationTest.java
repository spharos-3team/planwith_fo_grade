package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
import com.planwith.planwith_fo_grade.application.port.in.GrantGradeRewardUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeRewardHistoryPort;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeRewardHistory;
import com.planwith.planwith_fo_grade.domain.model.RewardStatus;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GrantGradeRewardServiceIntegrationTest {

	@Autowired
	private GrantGradeRewardUseCase grantGradeRewardUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private GradeRewardHistoryPort gradeRewardHistoryPort;

	@Autowired
	private SpringDataGradeOutboxRepository outboxRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void persistsRewardHistoryAndGradeRewardGrantedOutbox() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));

		grantGradeRewardUseCase.grant(new GrantGradeRewardCommand(
				memberUuid,
				GradeCode.EXPLORER.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		));

		GradeRewardHistory history = gradeRewardHistoryPort.findByMemberUuidAndRewardMonth(
				MemberUuid.from(memberUuid),
				"2026-08"
		).orElseThrow();
		assertThat(history.tokenAmount()).isEqualTo(50L);
		assertThat(history.rewardStatus()).isEqualTo(RewardStatus.COMPLETED);

		var unpublished = outboxRepository.findUnpublished(PageRequest.of(0, 10)).stream()
				.filter(outbox -> UUID.fromString(memberUuid).equals(outbox.aggregateUuid()))
				.toList();
		assertThat(unpublished).hasSize(1);
		assertThat(unpublished.get(0).eventType()).isEqualTo(GradeRewardGrantedEvent.EVENT_TYPE);
		JsonNode payload = objectMapper.readTree(unpublished.get(0).payload());
		assertThat(payload.get("tokenAmount").asLong()).isEqualTo(50L);
		assertThat(payload.get("gradeCode").asText()).isEqualTo("EXPLORER");
	}

	@Test
	void doesNotInsertSecondHistoryForSameMonth() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.LEAF).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		GrantGradeRewardCommand command = new GrantGradeRewardCommand(
				memberUuid,
				GradeCode.LEAF.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		);

		grantGradeRewardUseCase.grant(command);
		grantGradeRewardUseCase.grant(command);

		assertThat(gradeRewardHistoryPort.existsByMemberUuidAndRewardMonth(
				MemberUuid.from(memberUuid),
				"2026-08"
		)).isTrue();
		assertThat(outboxRepository.findUnpublished(PageRequest.of(0, 20)).stream()
				.filter(outbox -> UUID.fromString(memberUuid).equals(outbox.aggregateUuid()))
				.count()).isEqualTo(1);
	}
}
