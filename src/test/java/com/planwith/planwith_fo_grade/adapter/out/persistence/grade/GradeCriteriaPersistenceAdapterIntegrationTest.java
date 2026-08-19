package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.domain.model.BenefitCode;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCondition;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradeCriteriaPersistenceAdapterIntegrationTest {

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Test
	void loadsSeededGradeCriteriaFromDatabase() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());

		List<Grade> grades = gradeCriteriaPort.findAll();

		assertThat(grades).hasSize(6);
		assertThat(grades).extracting(Grade::gradeCode).containsExactly(
				GradeCode.ROOKIE,
				GradeCode.LEAF,
				GradeCode.TRAVELER,
				GradeCode.EXPLORER,
				GradeCode.ADVENTURE,
				GradeCode.PLANWITH
		);

		Grade rookie = gradeCriteriaPort.findByGradeCode(GradeCode.ROOKIE).orElseThrow();
		assertThat(rookie.gradeName()).isEqualTo("🌱 새싹");
		assertThat(rookie.conditions()).isEmpty();
		assertThat(rookie.benefits()).hasSize(1);
		assertThat(rookie.benefits().get(0).benefitCode()).isEqualTo(BenefitCode.MONTHLY_FREE_TOKEN);
		assertThat(rookie.benefits().get(0).benefitValue()).isEqualTo("10");

		Grade traveler = gradeCriteriaPort.findByGradeCode(GradeCode.TRAVELER).orElseThrow();
		assertThat(threshold(traveler, GradeMetricType.STORY_COUNT)).isEqualTo(10L);
		assertThat(threshold(traveler, GradeMetricType.FOLLOWER_COUNT)).isEqualTo(100L);
		assertThat(threshold(traveler, GradeMetricType.RECEIVED_LIKE_COUNT)).isEqualTo(500L);
		assertThat(traveler.benefits().get(0).benefitValue()).isEqualTo("30");

		Grade leaf = gradeCriteriaPort.findByGradeCode(GradeCode.LEAF).orElseThrow();
		assertThat(leaf.gradeName()).isEqualTo("🧳 잎새");

		Grade explorer = gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow();
		assertThat(explorer.benefits()).extracting(benefit -> benefit.benefitCode()).containsExactly(
				BenefitCode.MONTHLY_FREE_TOKEN,
				BenefitCode.PROFILE_BADGE,
				BenefitCode.MEMBERSHIP_PUBLIC_STORY
		);

		Grade adventure = gradeCriteriaPort.findByGradeCode(GradeCode.ADVENTURE).orElseThrow();
		assertThat(adventure.benefits()).extracting(benefit -> benefit.benefitCode()).containsExactly(
				BenefitCode.MONTHLY_FREE_TOKEN,
				BenefitCode.PROFILE_BADGE,
				BenefitCode.PROFILE_SPECIAL_BORDER,
				BenefitCode.NON_MEMBER_STORY_PRIORITY
		);
		assertThat(adventure.benefits().get(3).benefitValue()).isEqualTo("ADVENTURE");

		Grade planwith = gradeCriteriaPort.findByGradeCode(GradeCode.PLANWITH).orElseThrow();
		assertThat(planwith.benefits().get(3).benefitValue()).isEqualTo("PLANWITH");
	}

	@Test
	void syncsExistingBenefitsWhenInitializerRunsAgain() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		Grade explorer = gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow();
		gradeCriteriaPort.save(Grade.reconstitute(
				explorer.gradeId(),
				explorer.gradeCode(),
				explorer.gradeName(),
				explorer.gradeLevel(),
				explorer.description(),
				explorer.conditions(),
				List.of(explorer.benefits().get(0))
		));

		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());

		Grade synced = gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow();
		assertThat(synced.benefits()).extracting(benefit -> benefit.benefitCode()).containsExactly(
				BenefitCode.MONTHLY_FREE_TOKEN,
				BenefitCode.PROFILE_BADGE,
				BenefitCode.MEMBERSHIP_PUBLIC_STORY
		);
		assertThat(gradeCriteriaPort.findAll()).hasSize(6);
	}

	private static long threshold(Grade grade, GradeMetricType metricType) {
		Map<GradeMetricType, GradeCondition> conditions = grade.conditions().stream()
				.collect(Collectors.toMap(GradeCondition::metricType, Function.identity()));
		return conditions.get(metricType).thresholdValue();
	}
}
