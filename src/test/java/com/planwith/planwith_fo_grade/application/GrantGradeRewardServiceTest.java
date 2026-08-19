package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeOutboxMessage;
import com.planwith.planwith_fo_grade.application.port.out.GradeRewardHistoryPort;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeRewardHistory;
import com.planwith.planwith_fo_grade.domain.model.RewardStatus;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class GrantGradeRewardServiceTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String memberUuid = UUID.randomUUID().toString();
	private final LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);

	@Test
	void grantsExplorerMonthlyTokenAndStoresOutbox() throws Exception {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = assigned(criteriaPort, GradeCode.EXPLORER);
		InMemoryGradeRewardHistoryPort historyPort = new InMemoryGradeRewardHistoryPort();
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		GrantGradeRewardService service = new GrantGradeRewardService(
				memberPort, criteriaPort, historyPort, outboxPort, objectMapper
		);

		service.grant(new GrantGradeRewardCommand(
				memberUuid,
				GradeCode.EXPLORER.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		));

		GradeRewardHistory saved = historyPort.findByMemberUuidAndRewardMonth(
				MemberUuid.from(memberUuid),
				"2026-08"
		).orElseThrow();
		assertThat(saved.tokenAmount()).isEqualTo(50L);
		assertThat(saved.rewardStatus()).isEqualTo(RewardStatus.COMPLETED);
		assertThat(saved.gradeId()).isEqualTo(criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId());
		assertThat(outboxPort.messages).hasSize(1);

		GradeOutboxMessage message = outboxPort.messages.get(0);
		assertThat(message.aggregateType()).isEqualTo("GradeRewardHistory");
		assertThat(message.aggregateUuid()).isEqualTo(memberUuid);
		assertThat(message.eventType()).isEqualTo(GradeRewardGrantedEvent.EVENT_TYPE);
		JsonNode payload = objectMapper.readTree(message.payload());
		assertThat(payload.get("eventUuid").asText()).isEqualTo(message.eventUuid());
		assertThat(payload.get("memberUuid").asText()).isEqualTo(memberUuid);
		assertThat(payload.get("gradeCode").asText()).isEqualTo("EXPLORER");
		assertThat(payload.get("gradeLevel").asInt()).isEqualTo(4);
		assertThat(payload.get("rewardMonth").asText()).isEqualTo("2026-08");
		assertThat(payload.get("tokenAmount").asLong()).isEqualTo(50L);
		assertThat(payload.get("rewardType").asText()).isEqualTo("MONTHLY_FREE_TOKEN");
		assertThat(payload.get("grantedAt").asText()).isNotBlank();
		assertThat(payload.has("eventType")).isFalse();
	}

	@Test
	void skipsDuplicateRewardForSameMemberAndMonth() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeRewardHistoryPort historyPort = new InMemoryGradeRewardHistoryPort();
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		GrantGradeRewardService service = new GrantGradeRewardService(
				assigned(criteriaPort, GradeCode.EXPLORER),
				criteriaPort,
				historyPort,
				outboxPort,
				objectMapper
		);
		GrantGradeRewardCommand command = new GrantGradeRewardCommand(
				memberUuid,
				GradeCode.EXPLORER.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		);

		service.grant(command);
		service.grant(command);

		assertThat(historyPort.size()).isEqualTo(1);
		assertThat(outboxPort.messages).hasSize(1);
	}

	@ParameterizedTest(name = "{0} → {1}")
	@CsvSource({
			"ROOKIE, 10",
			"LEAF, 20",
			"TRAVELER, 30",
			"EXPLORER, 50",
			"ADVENTURE, 70",
			"PLANWITH, 120"
	})
	void grantsMonthlyTokenAmountByGrade(GradeCode gradeCode, long expectedTokenAmount) {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeRewardHistoryPort historyPort = new InMemoryGradeRewardHistoryPort();
		GrantGradeRewardService service = new GrantGradeRewardService(
				assigned(criteriaPort, gradeCode),
				criteriaPort,
				historyPort,
				new InMemoryGradeEventOutboxPort(),
				objectMapper
		);

		service.grant(new GrantGradeRewardCommand(
				memberUuid,
				gradeCode.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		));

		assertThat(historyPort.findByMemberUuidAndRewardMonth(MemberUuid.from(memberUuid), "2026-08")
				.orElseThrow()
				.tokenAmount()).isEqualTo(expectedTokenAmount);
	}

	@Test
	void skipsInactiveMember() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = assigned(criteriaPort, GradeCode.LEAF);
		memberPort.save(memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow().suspend());
		InMemoryGradeRewardHistoryPort historyPort = new InMemoryGradeRewardHistoryPort();
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		GrantGradeRewardService service = new GrantGradeRewardService(
				memberPort, criteriaPort, historyPort, outboxPort, objectMapper
		);

		service.grant(new GrantGradeRewardCommand(
				memberUuid,
				GradeCode.LEAF.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		));

		assertThat(historyPort.size()).isZero();
		assertThat(outboxPort.messages).isEmpty();
	}

	@Test
	void skipsMissingMember() {
		InMemoryGradeRewardHistoryPort historyPort = new InMemoryGradeRewardHistoryPort();
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		GrantGradeRewardService service = new GrantGradeRewardService(
				new InMemoryGradeMemberPort(),
				InMemoryGradeCriteriaPort.withCatalog(),
				historyPort,
				outboxPort,
				objectMapper
		);

		service.grant(new GrantGradeRewardCommand(
				memberUuid,
				GradeCode.ROOKIE.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		));

		assertThat(historyPort.size()).isZero();
		assertThat(outboxPort.messages).isEmpty();
	}

	@Test
	void skipsUnsupportedRewardType() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeRewardHistoryPort historyPort = new InMemoryGradeRewardHistoryPort();
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		GrantGradeRewardService service = new GrantGradeRewardService(
				assigned(criteriaPort, GradeCode.PLANWITH),
				criteriaPort,
				historyPort,
				outboxPort,
				objectMapper
		);

		service.grant(new GrantGradeRewardCommand(memberUuid, "PLANWITH", "PROFILE_BADGE", "2026-08"));

		assertThat(historyPort.size()).isZero();
		assertThat(outboxPort.messages).isEmpty();
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
			return grades.values().stream().min(Comparator.comparingInt(Grade::gradeLevel));
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
		public List<GradeMember> findAllActive() {
			return members.values().stream().filter(GradeMember::isActive).toList();
		}

		@Override
		public GradeMember save(GradeMember member) {
			members.put(member.memberUuid().value(), member);
			return member;
		}
	}

	private static final class InMemoryGradeRewardHistoryPort implements GradeRewardHistoryPort {

		private final Map<String, GradeRewardHistory> histories = new LinkedHashMap<>();
		private long nextId = 1L;

		@Override
		public boolean existsByMemberUuidAndRewardMonth(MemberUuid memberUuid, String rewardMonth) {
			return histories.containsKey(key(memberUuid, rewardMonth));
		}

		@Override
		public Optional<GradeRewardHistory> findByMemberUuidAndRewardMonth(MemberUuid memberUuid, String rewardMonth) {
			return Optional.ofNullable(histories.get(key(memberUuid, rewardMonth)));
		}

		@Override
		public GradeRewardHistory save(GradeRewardHistory history) {
			GradeRewardHistory persisted = history.rewardId() == null
					? GradeRewardHistory.reconstitute(
							nextId++,
							history.memberUuid(),
							history.gradeId(),
							history.rewardMonth(),
							history.tokenAmount(),
							history.rewardStatus(),
							history.createdAt()
					)
					: history;
			histories.put(key(persisted.memberUuid(), persisted.rewardMonth()), persisted);
			return persisted;
		}

		int size() {
			return histories.size();
		}

		private static String key(MemberUuid memberUuid, String rewardMonth) {
			return memberUuid + ":" + rewardMonth;
		}
	}

	private static final class InMemoryGradeEventOutboxPort implements GradeEventOutboxPort {

		private final List<GradeOutboxMessage> messages = new ArrayList<>();

		@Override
		public void save(GradeOutboxMessage message) {
			messages.add(message);
		}
	}
}
