package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.query.GradeCatalogView;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;

class GetAllGradesServiceTest {

	@Test
	void listsAllGradesWithConditionsAndBenefits() {
		GetAllGradesService service = new GetAllGradesService(InMemoryGradeCriteriaPort.withCatalog());

		List<GradeCatalogView> grades = service.listAll();

		assertThat(grades).extracting(GradeCatalogView::gradeCode).containsExactly(
				"ROOKIE", "LEAF", "TRAVELER", "EXPLORER", "ADVENTURE", "PLANWITH"
		);
		assertThat(grades.get(0).conditions()).isEmpty();
		assertThat(grades.get(0).benefits()).hasSize(1);
		assertThat(grades.get(0).benefits().get(0).benefitValue()).isEqualTo("10");

		GradeCatalogView traveler = grades.get(2);
		assertThat(traveler.gradeLevel()).isEqualTo(3);
		assertThat(traveler.conditions()).extracting(condition -> condition.metricType()).containsExactly(
				GradeMetricType.STORY_COUNT.name(),
				GradeMetricType.FOLLOWER_COUNT.name(),
				GradeMetricType.RECEIVED_LIKE_COUNT.name()
		);
		assertThat(traveler.conditions()).extracting(condition -> condition.thresholdValue())
				.containsExactly(10L, 100L, 500L);
		assertThat(traveler.benefits().get(0).benefitCode()).isEqualTo("MONTHLY_FREE_TOKEN");
		assertThat(grades.get(1).gradeName()).isEqualTo("🧳 잎새");
		assertThat(grades.get(3).benefits()).extracting(benefit -> benefit.benefitCode()).containsExactly(
				"MONTHLY_FREE_TOKEN",
				"PROFILE_BADGE",
				"MEMBERSHIP_PUBLIC_STORY"
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
}
