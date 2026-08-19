package com.planwith.planwith_fo_grade.application.port.out;

import java.util.List;
import java.util.Optional;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;

public interface GradeCriteriaPort {

	List<Grade> findAll();

	Optional<Grade> findByGradeCode(GradeCode gradeCode);

	Optional<Grade> findLowestGrade();

	Grade save(Grade grade);
}
