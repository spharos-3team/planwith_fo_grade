package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_grade.domain.model.GradeCode;

interface SpringDataGradeRepository extends JpaRepository<GradeJpaEntity, Long> {

	Optional<GradeJpaEntity> findByGradeCode(GradeCode gradeCode);

	List<GradeJpaEntity> findAllByOrderByGradeLevelAsc();
}
