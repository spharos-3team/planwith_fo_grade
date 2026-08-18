package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGradeRepository extends JpaRepository<GradeJpaEntity, Long> {

	Optional<GradeJpaEntity> findByGradeCode(com.planwith.planwith_fo_grade.domain.model.GradeCode gradeCode);
}
