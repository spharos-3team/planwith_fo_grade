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
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.adapter.out.redis.InMemoryGradeQueryCacheAdapter;
import com.planwith.planwith_fo_grade.application.command.EvaluateGradeCommand;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeOutboxMessage;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class EvaluateGradeServiceTest {

	private final String memberUuid = UUID.randomUUID().toString();
	private final LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);

	@Test
	void promotesRookieToExplorerWhenAllExplorerThresholdsAreMet() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = assignedRookie(criteriaPort);
		InMemoryMemberGradeMetricPort metricPort = new InMemoryMemberGradeMetricPort();
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		metricPort.save(metric(MemberMetricType.STORY_COUNT, 35L, "story-service"));
		metricPort.save(metric(MemberMetricType.FOLLOWER_COUNT, 1_500L, "follow-service"));
		metricPort.save(metric(MemberMetricType.RECEIVED_LIKE_COUNT, 6_200L, "like-service"));
		EvaluateGradeService service = service(memberPort, criteriaPort, metricPort, outboxPort);

		service.evaluate(new EvaluateGradeCommand(memberUuid));

		GradeMember saved = memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId());
		assertThat(saved.lastEvaluatedAt()).isNotNull();
		assertThat(outboxPort.messages).hasSize(1);
	}

	@Test
	void keepsCurrentGradeWhenMetricsDecrease() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		Grade explorer = criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow();
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(explorer.gradeId(), MemberUuid.from(memberUuid), assignedAt));
		InMemoryMemberGradeMetricPort metricPort = new InMemoryMemberGradeMetricPort();
		InMemoryGradeEventOutboxPort outboxPort = new InMemoryGradeEventOutboxPort();
		EvaluateGradeService service = service(memberPort, criteriaPort, metricPort, outboxPort);

		service.evaluate(new EvaluateGradeCommand(memberUuid));

		GradeMember saved = memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(explorer.gradeId());
		assertThat(saved.lastEvaluatedAt()).isNotNull();
		assertThat(outboxPort.messages).isEmpty();
	}

	@Test
	void skipsEvaluationWhenMemberGradeIsMissing() {
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		EvaluateGradeService service = service(
				memberPort,
				InMemoryGradeCriteriaPort.withCatalog(),
				new InMemoryMemberGradeMetricPort(),
				new InMemoryGradeEventOutboxPort()
		);

		service.evaluate(new EvaluateGradeCommand(memberUuid));

		assertThat(memberPort.saveCount).isZero();
	}

	@Test
	void skipsEvaluationWhenMemberGradeIsNotActive() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = assignedRookie(criteriaPort);
		memberPort.save(memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow().suspend());
		int saveCountAfterSuspend = memberPort.saveCount;
		EvaluateGradeService service = service(
				memberPort,
				criteriaPort,
				new InMemoryMemberGradeMetricPort(),
				new InMemoryGradeEventOutboxPort()
		);

		service.evaluate(new EvaluateGradeCommand(memberUuid));

		assertThat(memberPort.saveCount).isEqualTo(saveCountAfterSuspend);
		assertThat(memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow().gradeId())
				.isEqualTo(criteriaPort.findByGradeCode(GradeCode.ROOKIE).orElseThrow().gradeId());
	}

	@Test
	void ignoresPostCountWhenEvaluatingGrade() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		InMemoryGradeMemberPort memberPort = assignedRookie(criteriaPort);
		InMemoryMemberGradeMetricPort metricPort = new InMemoryMemberGradeMetricPort();
		metricPort.save(metric(MemberMetricType.STORY_COUNT, 35L, "story-service"));
		metricPort.save(metric(MemberMetricType.FOLLOWER_COUNT, 1_500L, "follow-service"));
		metricPort.save(metric(MemberMetricType.RECEIVED_LIKE_COUNT, 6_200L, "like-service"));
		metricPort.save(metric(MemberMetricType.POST_COUNT, 0L, "story-service"));
		EvaluateGradeService service = service(memberPort, criteriaPort, metricPort, new InMemoryGradeEventOutboxPort());

		service.evaluate(new EvaluateGradeCommand(memberUuid));

		assertThat(memberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow().gradeId())
				.isEqualTo(criteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId());
	}

	private InMemoryGradeMemberPort assignedRookie(InMemoryGradeCriteriaPort criteriaPort) {
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(
				criteriaPort.findLowestGrade().orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		return memberPort;
	}

	private EvaluateGradeService service(
			InMemoryGradeMemberPort memberPort,
			InMemoryGradeCriteriaPort criteriaPort,
			InMemoryMemberGradeMetricPort metricPort,
			InMemoryGradeEventOutboxPort outboxPort
	) {
		return new EvaluateGradeService(
				memberPort,
				criteriaPort,
				metricPort,
				new ChangeMemberGradeService(
						memberPort,
						criteriaPort,
						outboxPort,
						new InMemoryGradeQueryCacheAdapter(),
						new ObjectMapper()
				)
		);
	}

	private MemberGradeMetric metric(MemberMetricType metricType, long currentValue, String sourceService) {
		return MemberGradeMetric.reconstitute(
				1L,
				MemberUuid.from(memberUuid),
				metricType,
				currentValue,
				sourceService,
				1L,
				assignedAt
		);
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

	private static final class InMemoryMemberGradeMetricPort implements MemberGradeMetricPort {

		private final Map<String, MemberGradeMetric> metrics = new LinkedHashMap<>();
		private final AtomicLong nextId = new AtomicLong(1L);

		@Override
		public Optional<MemberGradeMetric> findByMemberUuidAndMetricType(
				MemberUuid memberUuid,
				MemberMetricType metricType
		) {
			return Optional.ofNullable(metrics.get(key(memberUuid, metricType)));
		}

		@Override
		public List<MemberGradeMetric> findByMemberUuid(MemberUuid memberUuid) {
			return metrics.values().stream()
					.filter(metric -> metric.memberUuid().equals(memberUuid))
					.toList();
		}

		@Override
		public MemberGradeMetric save(MemberGradeMetric metric) {
			MemberGradeMetric persisted = metric.metricId() == null
					? MemberGradeMetric.reconstitute(
							nextId.getAndIncrement(),
							metric.memberUuid(),
							metric.metricType(),
							metric.currentValue(),
							metric.sourceService(),
							metric.sourceVersion(),
							metric.synchronizedAt()
					)
					: metric;
			metrics.put(key(persisted.memberUuid(), persisted.metricType()), persisted);
			return persisted;
		}

		private static String key(MemberUuid memberUuid, MemberMetricType metricType) {
			return memberUuid + ":" + metricType;
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
