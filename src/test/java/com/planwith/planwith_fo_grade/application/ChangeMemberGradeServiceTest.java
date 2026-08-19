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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.adapter.out.redis.InMemoryGradeQueryCacheAdapter;
import com.planwith.planwith_fo_grade.application.command.ChangeMemberGradeCommand;
import com.planwith.planwith_fo_grade.application.event.GradeChangedEvent;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeOutboxMessage;
import com.planwith.planwith_fo_grade.application.query.CurrentBenefitSummaryView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class ChangeMemberGradeServiceTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String memberUuid = UUID.randomUUID().toString();
	private final LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);

	@Test
	void updatesGradeMemberAndStoresGradeChangedOutbox() throws Exception {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(
				criteriaPort.findByGradeCode(GradeCode.TRAVELER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		InMemoryGradeQueryCacheAdapter cache = new InMemoryGradeQueryCacheAdapter();
		cache.save(memberUuid, cachedView());
		ChangeMemberGradeService service = new ChangeMemberGradeService(
				memberPort, criteriaPort, outboxPort, cache, objectMapper
		);

		service.change(new ChangeMemberGradeCommand(
				memberUuid,
				GradeCode.TRAVELER.name(),
				GradeCode.EXPLORER.name()
		));

		GradeMember saved = memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId());
		assertThat(saved.lastEvaluatedAt()).isNotNull();
		assertThat(outboxPort.messages).hasSize(1);
		assertThat(cache.contains(memberUuid)).isFalse();

		GradeOutboxMessage message = outboxPort.messages.get(0);
		assertThat(message.aggregateType()).isEqualTo("GradeMember");
		assertThat(message.aggregateUuid()).isEqualTo(memberUuid);
		assertThat(message.eventType()).isEqualTo(GradeChangedEvent.EVENT_TYPE);
		JsonNode payload = objectMapper.readTree(message.payload());
		assertThat(payload.get("eventUuid").asText()).isEqualTo(message.eventUuid());
		assertThat(payload.get("memberUuid").asText()).isEqualTo(memberUuid);
		assertThat(payload.get("previousGradeCode").asText()).isEqualTo(GradeCode.TRAVELER.name());
		assertThat(payload.get("currentGradeCode").asText()).isEqualTo(GradeCode.EXPLORER.name());
		assertThat(payload.get("previousGradeLevel").asInt()).isEqualTo(
				criteriaPort.findByGradeCode(GradeCode.TRAVELER).orElseThrow().gradeLevel()
		);
		assertThat(payload.get("currentGradeLevel").asInt()).isEqualTo(
				criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeLevel()
		);
		assertThat(payload.get("changedAt").asText()).isNotBlank();
		assertThat(payload.has("eventType")).isFalse();
		assertThat(payload.has("previousGrade")).isFalse();
		assertThat(payload.has("fromGradeCode")).isFalse();
	}

	@Test
	void propagatesOutboxInsertFailure() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(
				criteriaPort.findByGradeCode(GradeCode.TRAVELER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		ChangeMemberGradeService service = new ChangeMemberGradeService(
				memberPort,
				criteriaPort,
				message -> {
					throw new IllegalStateException("Outbox INSERT 실패");
				},
				new InMemoryGradeQueryCacheAdapter(),
				objectMapper
		);

		assertThatThrownBy(() -> service.change(new ChangeMemberGradeCommand(
				memberUuid,
				GradeCode.TRAVELER.name(),
				GradeCode.EXPLORER.name()
		))).isInstanceOf(IllegalStateException.class)
				.hasMessage("Outbox INSERT 실패");
	}

	@Test
	void rejectsDemotion() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(
				criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		ChangeMemberGradeService service = new ChangeMemberGradeService(
				memberPort, criteriaPort, outboxPort, new InMemoryGradeQueryCacheAdapter(), objectMapper
		);

		assertThatThrownBy(() -> service.change(new ChangeMemberGradeCommand(
				memberUuid,
				GradeCode.EXPLORER.name(),
				GradeCode.TRAVELER.name()
		))).isInstanceOf(InvalidGradeException.class);
		assertThat(outboxPort.messages).isEmpty();
		assertThat(memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow().gradeId())
				.isEqualTo(criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId());
	}

	@Test
	void doesNotEvictCacheWhenGradeChangeIsRejected() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(
				criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		InMemoryGradeQueryCacheAdapter cache = new InMemoryGradeQueryCacheAdapter();
		cache.save(memberUuid, cachedView());
		ChangeMemberGradeService service = new ChangeMemberGradeService(
				memberPort, criteriaPort, new InMemoryGradeEventOutboxPort(), cache, objectMapper
		);

		assertThatThrownBy(() -> service.change(new ChangeMemberGradeCommand(
				memberUuid,
				GradeCode.EXPLORER.name(),
				GradeCode.TRAVELER.name()
		))).isInstanceOf(InvalidGradeException.class);
		assertThat(cache.contains(memberUuid)).isTrue();
	}

	private GradeManagementView cachedView() {
		return new GradeManagementView(
				new GradeManagementView.CurrentGradeView("TRAVELER", "✈️ 여행가", 3, List.of()),
				new GradeManagementView.CurrentMetricsView(0L, 0L, 0L),
				new GradeManagementView.NextGradeView("EXPLORER", "🧭 탐험가", List.of()),
				new GradeManagementView.ProgressView(
						new GradeManagementView.MetricProgressView(0L, 30L, 30L, 0),
						new GradeManagementView.MetricProgressView(0L, 1_000L, 1_000L, 0),
						new GradeManagementView.MetricProgressView(0L, 5_000L, 5_000L, 0)
				),
				new CurrentBenefitSummaryView(
						"TRAVELER",
						"✈️ 여행가",
						3,
						30,
						false,
						false,
						false,
						false,
						null
				)
		);
	}

	@Test
	void rejectsMissingMember() {
		ChangeMemberGradeService service = new ChangeMemberGradeService(
				new InMemoryGradeMemberPort(),
				InMemoryGradeCriteriaPort.withCatalog(),
				new InMemoryGradeEventOutboxPort(),
				new InMemoryGradeQueryCacheAdapter(),
				objectMapper
		);

		assertThatThrownBy(() -> service.change(new ChangeMemberGradeCommand(
				memberUuid,
				GradeCode.TRAVELER.name(),
				GradeCode.EXPLORER.name()
		))).isInstanceOf(InvalidGradeException.class);
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

	private static final class InMemoryGradeEventOutboxPort implements GradeEventOutboxPort {

		private final List<GradeOutboxMessage> messages = new ArrayList<>();

		@Override
		public void save(GradeOutboxMessage message) {
			messages.add(message);
		}
	}
}
