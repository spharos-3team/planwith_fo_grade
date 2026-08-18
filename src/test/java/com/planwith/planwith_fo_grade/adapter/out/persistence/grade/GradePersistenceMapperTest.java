package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.model.BenefitCode;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeBenefit;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCondition;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;
import com.planwith.planwith_fo_grade.domain.model.GradeRewardHistory;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.RewardStatus;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class GradePersistenceMapperTest {

	@Test
	void mapsGradeAggregateBetweenDomainAndEntity() {
		Grade domain = Grade.reconstitute(
				1L,
				GradeCode.LEAF,
				"Leaf",
				2,
				"초보 등급",
				List.of(GradeCondition.reconstitute(
						10L, 1L, GradeMetricType.STORY_COUNT, "스토리 10개", 10L, 1, null
				)),
				List.of(GradeBenefit.reconstitute(
						20L, 1L, BenefitCode.MONTHLY_FREE_TOKEN, "월간 토큰", "100", null, 1
				))
		);

		GradeJpaEntity entity = GradePersistenceMapper.toEntity(domain);
		Grade mapped = GradePersistenceMapper.toDomain(entity);

		assertThat(mapped.gradeCode()).isEqualTo(GradeCode.LEAF);
		assertThat(mapped.gradeName()).isEqualTo("Leaf");
		assertThat(mapped.conditions()).hasSize(1);
		assertThat(mapped.benefits()).hasSize(1);
		assertThat(mapped.conditions().get(0).metricType()).isEqualTo(GradeMetricType.STORY_COUNT);
	}

	@Test
	void mapsGradeMemberBetweenDomainAndEntity() {
		MemberUuid memberUuid = MemberUuid.from(UUID.randomUUID().toString());
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 18, 10, 0);
		GradeMember domain = GradeMember.assign(1L, memberUuid, assignedAt);

		GradeMemberJpaEntity entity = GradePersistenceMapper.toEntity(domain);
		GradeMember mapped = GradePersistenceMapper.toDomain(entity);

		assertThat(mapped.memberUuid()).isEqualTo(memberUuid);
		assertThat(mapped.gradeId()).isEqualTo(1L);
		assertThat(mapped.gradeAssignedAt()).isEqualTo(assignedAt);
	}

	@Test
	void mapsMemberGradeMetricBetweenDomainAndEntity() {
		MemberUuid memberUuid = MemberUuid.from(UUID.randomUUID().toString());
		LocalDateTime syncedAt = LocalDateTime.of(2026, 8, 18, 10, 0);
		MemberGradeMetric domain = MemberGradeMetric.initialize(
				memberUuid, MemberMetricType.POST_COUNT, "story-service", syncedAt
		);

		MemberGradeMetricJpaEntity entity = GradePersistenceMapper.toEntity(domain);
		MemberGradeMetric mapped = GradePersistenceMapper.toDomain(entity);

		assertThat(mapped.metricType()).isEqualTo(MemberMetricType.POST_COUNT);
		assertThat(mapped.currentValue()).isZero();
		assertThat(mapped.sourceService()).isEqualTo("story-service");
	}

	@Test
	void mapsGradeRewardHistoryBetweenDomainAndEntity() {
		MemberUuid memberUuid = MemberUuid.from(UUID.randomUUID().toString());
		LocalDateTime createdAt = LocalDateTime.of(2026, 8, 18, 0, 0);
		GradeRewardHistory domain = GradeRewardHistory.createReady(
				memberUuid, 2L, "2026-08", 100L, createdAt
		);

		GradeRewardHistoryJpaEntity entity = GradePersistenceMapper.toEntity(domain);
		GradeRewardHistory mapped = GradePersistenceMapper.toDomain(entity);

		assertThat(mapped.rewardMonth()).isEqualTo("2026-08");
		assertThat(mapped.tokenAmount()).isEqualTo(100L);
		assertThat(mapped.rewardStatus()).isEqualTo(RewardStatus.READY);
	}
}
