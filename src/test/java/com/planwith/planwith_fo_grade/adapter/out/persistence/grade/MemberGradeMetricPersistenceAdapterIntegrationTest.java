package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;
import com.planwith.planwith_fo_grade.application.port.in.RecordGradeMetricUseCase;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.application.port.out.ProcessedGradeEventPort;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberGradeMetricPersistenceAdapterIntegrationTest {

	@Autowired
	private RecordGradeMetricUseCase recordGradeMetricUseCase;

	@Autowired
	private MemberGradeMetricPort memberGradeMetricPort;

	@Autowired
	private ProcessedGradeEventPort processedGradeEventPort;

	@Test
	void persistsStoryCountProjection() {
		String memberUuid = UUID.randomUUID().toString();

		recordGradeMetricUseCase.record(command(
				UUID.randomUUID().toString(),
				memberUuid,
				MemberMetricType.STORY_COUNT.name(),
				1L,
				null
		));
		recordGradeMetricUseCase.record(command(
				UUID.randomUUID().toString(),
				memberUuid,
				MemberMetricType.STORY_COUNT.name(),
				1L,
				null
		));

		MemberGradeMetric saved = memberGradeMetricPort.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow();
		assertThat(saved.currentValue()).isEqualTo(2L);
		assertThat(saved.sourceService()).isEqualTo("story-service");
		assertThat(saved.sourceVersion()).isEqualTo(2L);
	}

	@Test
	void doesNotApplyDuplicateEventUuid() {
		String memberUuid = UUID.randomUUID().toString();
		String eventUuid = UUID.randomUUID().toString();

		recordGradeMetricUseCase.record(command(
				eventUuid,
				memberUuid,
				MemberMetricType.STORY_COUNT.name(),
				1L,
				null
		));
		recordGradeMetricUseCase.record(command(
				eventUuid,
				memberUuid,
				MemberMetricType.STORY_COUNT.name(),
				1L,
				null
		));

		assertThat(memberGradeMetricPort.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow().currentValue()).isEqualTo(1L);
		assertThat(processedGradeEventPort.existsByEventUuid(UUID.fromString(eventUuid))).isTrue();
	}

	@Test
	void doesNotApplyOlderSourceVersion() {
		String memberUuid = UUID.randomUUID().toString();

		recordGradeMetricUseCase.record(command(
				UUID.randomUUID().toString(),
				memberUuid,
				MemberMetricType.FOLLOWER_COUNT.name(),
				1L,
				16L
		));
		recordGradeMetricUseCase.record(command(
				UUID.randomUUID().toString(),
				memberUuid,
				MemberMetricType.FOLLOWER_COUNT.name(),
				1L,
				15L
		));

		MemberGradeMetric saved = memberGradeMetricPort.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.FOLLOWER_COUNT
		).orElseThrow();
		assertThat(saved.currentValue()).isEqualTo(1L);
		assertThat(saved.sourceVersion()).isEqualTo(16L);
	}

	private static RecordGradeMetricCommand command(
			String eventUuid,
			String memberUuid,
			String metricType,
			long delta,
			Long sourceVersion
	) {
		return new RecordGradeMetricCommand(eventUuid, memberUuid, metricType, delta, sourceVersion);
	}
}
