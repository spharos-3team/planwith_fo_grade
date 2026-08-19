package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.command.AssignInitialGradeCommand;
import com.planwith.planwith_fo_grade.application.command.EvaluateGradeCommand;
import com.planwith.planwith_fo_grade.application.port.in.AssignInitialGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.in.EvaluateGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EvaluateGradeServiceIntegrationTest {

	@Autowired
	private AssignInitialGradeUseCase assignInitialGradeUseCase;

	@Autowired
	private EvaluateGradeUseCase evaluateGradeUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private MemberGradeMetricPort memberGradeMetricPort;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void promotesRookieToExplorerWhenAllExplorerThresholdsAreMet() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		assignInitialGradeUseCase.assign(new AssignInitialGradeCommand(memberUuid, assignedAt));
		saveMetric(memberUuid, MemberMetricType.STORY_COUNT, 35L, "story-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.FOLLOWER_COUNT, 1_500L, "follow-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT, 6_200L, "like-service", assignedAt);

		evaluateGradeUseCase.evaluate(new EvaluateGradeCommand(memberUuid));

		GradeMember saved = gradeMemberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(
				gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId()
		);
		assertThat(saved.lastEvaluatedAt()).isNotNull();
		assertThat(outboxCount(memberUuid)).isEqualTo(1);
		assertThat(outboxPayload(memberUuid)).contains(
				"\"fromGradeCode\":\"ROOKIE\"",
				"\"toGradeCode\":\"EXPLORER\"",
				"\"eventType\":\"GradeChanged\""
		);
	}

	@Test
	void doesNotDemoteWhenMetricsFallBelowCurrentGrade() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		assignInitialGradeUseCase.assign(new AssignInitialGradeCommand(memberUuid, assignedAt));
		saveMetric(memberUuid, MemberMetricType.STORY_COUNT, 35L, "story-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.FOLLOWER_COUNT, 1_500L, "follow-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT, 6_200L, "like-service", assignedAt);
		evaluateGradeUseCase.evaluate(new EvaluateGradeCommand(memberUuid));

		saveMetric(memberUuid, MemberMetricType.STORY_COUNT, 0L, "story-service", assignedAt.plusHours(1));
		saveMetric(memberUuid, MemberMetricType.FOLLOWER_COUNT, 0L, "follow-service", assignedAt.plusHours(1));
		saveMetric(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT, 0L, "like-service", assignedAt.plusHours(1));
		evaluateGradeUseCase.evaluate(new EvaluateGradeCommand(memberUuid));

		GradeMember saved = gradeMemberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(
				gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId()
		);
		assertThat(saved.lastEvaluatedAt()).isNotNull();
		assertThat(outboxCount(memberUuid)).isEqualTo(1);
	}

	private long outboxCount(String memberUuid) {
		Long count = jdbcTemplate.queryForObject(
				"select count(*) from grade_outbox where aggregate_uuid = ?",
				Long.class,
				memberUuid
		);
		return count == null ? 0L : count;
	}

	private String outboxPayload(String memberUuid) {
		return jdbcTemplate.queryForObject(
				"select payload from grade_outbox where aggregate_uuid = ?",
				String.class,
				memberUuid
		);
	}

	private void saveMetric(
			String memberUuid,
			MemberMetricType metricType,
			long currentValue,
			String sourceService,
			LocalDateTime synchronizedAt
	) {
		MemberUuid uuid = MemberUuid.from(memberUuid);
		MemberGradeMetric current = memberGradeMetricPort.findByMemberUuidAndMetricType(uuid, metricType)
				.orElseGet(() -> MemberGradeMetric.initialize(uuid, metricType, sourceService, synchronizedAt));
		memberGradeMetricPort.save(current.synchronize(
				currentValue,
				sourceService,
				current.sourceVersion() + 1,
				synchronizedAt
		));
	}
}
