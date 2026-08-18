package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;

interface SpringDataMemberGradeMetricRepository extends JpaRepository<MemberGradeMetricJpaEntity, Long> {

	Optional<MemberGradeMetricJpaEntity> findByMemberUuidAndMetricType(UUID memberUuid, MemberMetricType metricType);

	List<MemberGradeMetricJpaEntity> findByMemberUuid(UUID memberUuid);
}
