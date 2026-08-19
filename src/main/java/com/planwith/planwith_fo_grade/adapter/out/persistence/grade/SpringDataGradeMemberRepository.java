package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_grade.domain.model.GradeStatus;

interface SpringDataGradeMemberRepository extends JpaRepository<GradeMemberJpaEntity, UUID> {

	List<GradeMemberJpaEntity> findByGradeStatus(GradeStatus gradeStatus);
}
