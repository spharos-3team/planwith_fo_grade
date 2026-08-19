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

	@Test
	void persistsStoryCountProjection() {
		String memberUuid = UUID.randomUUID().toString();

		recordGradeMetricUseCase.record(new RecordGradeMetricCommand(
				memberUuid,
				MemberMetricType.STORY_COUNT.name(),
				1L
		));
		recordGradeMetricUseCase.record(new RecordGradeMetricCommand(
				memberUuid,
				MemberMetricType.STORY_COUNT.name(),
				1L
		));

		MemberGradeMetric saved = memberGradeMetricPort.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow();
		assertThat(saved.currentValue()).isEqualTo(2L);
		assertThat(saved.sourceService()).isEqualTo("story-service");
		assertThat(saved.sourceVersion()).isEqualTo(2L);
	}
}
