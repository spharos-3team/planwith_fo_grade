package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.domain.model.BenefitCode;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeBenefit;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCondition;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradeJpaEntityIntegrationTest {

	@Autowired
	private SpringDataGradeRepository gradeRepository;

	@Autowired
	private SpringDataGradeMemberRepository gradeMemberRepository;

	@Test
	void persistsGradeAggregateAndMember() {
		Grade grade = Grade.create(
				GradeCode.ROOKIE,
				"Rookie",
				1,
				"입문 등급",
				List.of(GradeCondition.create(
						0L, GradeMetricType.STORY_COUNT, "스토리 1개", 1L, 1, null
				)),
				List.of(GradeBenefit.create(
						0L, BenefitCode.MONTHLY_FREE_TOKEN, "월간 토큰", "10", null, 1
				))
		);

		GradeJpaEntity savedGrade = gradeRepository.save(GradePersistenceMapper.toEntity(grade));
		Grade loaded = GradePersistenceMapper.toDomain(
				gradeRepository.findById(savedGrade.getGradeId()).orElseThrow()
		);

		assertThat(loaded.gradeCode()).isEqualTo(GradeCode.ROOKIE);
		assertThat(loaded.conditions()).hasSize(1);
		assertThat(loaded.benefits()).hasSize(1);
		assertThat(loaded.conditions().get(0).gradeId()).isEqualTo(savedGrade.getGradeId());

		MemberUuid memberUuid = MemberUuid.from(UUID.randomUUID().toString());
		GradeMember member = GradeMember.assign(savedGrade.getGradeId(), memberUuid, java.time.LocalDateTime.now());
		gradeMemberRepository.save(GradePersistenceMapper.toEntity(member));

		assertThat(gradeMemberRepository.findById(memberUuid.value())).isPresent();
	}
}
