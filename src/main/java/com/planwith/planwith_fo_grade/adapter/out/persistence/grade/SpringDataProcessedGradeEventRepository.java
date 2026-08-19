package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataProcessedGradeEventRepository extends JpaRepository<ProcessedGradeEventJpaEntity, Long> {

	boolean existsByEventUuid(UUID eventUuid);
}
