package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;

class GradeCriteriaInitializerTest {

	@Test
	void seedsSixGradesWhenEmpty() {
		InMemoryGradeCriteriaPort port = new InMemoryGradeCriteriaPort();
		GradeCriteriaInitializer initializer = new GradeCriteriaInitializer(port);

		initializer.run(new DefaultApplicationArguments());

		assertThat(port.findAll()).extracting(Grade::gradeCode).containsExactly(
				GradeCode.ROOKIE,
				GradeCode.LEAF,
				GradeCode.TRAVELER,
				GradeCode.EXPLORER,
				GradeCode.ADVENTURE,
				GradeCode.PLANWITH
		);
	}

	@Test
	void doesNotDuplicateExistingGrades() {
		InMemoryGradeCriteriaPort port = new InMemoryGradeCriteriaPort();
		GradeCriteriaInitializer initializer = new GradeCriteriaInitializer(port);

		initializer.run(new DefaultApplicationArguments());
		initializer.run(new DefaultApplicationArguments());

		assertThat(port.findAll()).hasSize(6);
		assertThat(port.findByGradeCode(GradeCode.LEAF).orElseThrow().gradeName()).isEqualTo("🧳 잎새");
		assertThat(port.findByGradeCode(GradeCode.EXPLORER).orElseThrow().benefits()).hasSize(3);
	}

	@Test
	void updatesExistingGradeNameAndBenefits() {
		InMemoryGradeCriteriaPort port = new InMemoryGradeCriteriaPort();
		GradeCriteriaInitializer initializer = new GradeCriteriaInitializer(port);
		initializer.run(new DefaultApplicationArguments());
		port.save(Grade.create(
				GradeCode.LEAF,
				"🌿 잎새",
				2,
				"old",
				List.of(),
				List.of()
		));

		initializer.run(new DefaultApplicationArguments());

		Grade leaf = port.findByGradeCode(GradeCode.LEAF).orElseThrow();
		assertThat(leaf.gradeName()).isEqualTo("🧳 잎새");
		assertThat(leaf.benefits()).hasSize(1);
		assertThat(port.findAll()).hasSize(6);
	}

	private static final class InMemoryGradeCriteriaPort implements GradeCriteriaPort {

		private final Map<GradeCode, Grade> grades = new LinkedHashMap<>();

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
