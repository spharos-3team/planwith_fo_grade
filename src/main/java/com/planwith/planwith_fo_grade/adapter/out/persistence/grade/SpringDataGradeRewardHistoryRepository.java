package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGradeRewardHistoryRepository extends JpaRepository<GradeRewardHistoryJpaEntity, Long> {

	boolean existsByMemberUuidAndRewardMonth(UUID memberUuid, String rewardMonth);

	Optional<GradeRewardHistoryJpaEntity> findByMemberUuidAndRewardMonth(UUID memberUuid, String rewardMonth);
}
