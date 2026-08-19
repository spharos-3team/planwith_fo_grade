package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@Component
public class MemberGradeMetricPersistenceAdapter implements MemberGradeMetricPort {

	private final SpringDataMemberGradeMetricRepository memberGradeMetricRepository;

	public MemberGradeMetricPersistenceAdapter(SpringDataMemberGradeMetricRepository memberGradeMetricRepository) {
		this.memberGradeMetricRepository = memberGradeMetricRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MemberGradeMetric> findByMemberUuidAndMetricType(
			MemberUuid memberUuid,
			MemberMetricType metricType
	) {
		return memberGradeMetricRepository.findByMemberUuidAndMetricType(memberUuid.value(), metricType)
				.map(GradePersistenceMapper::toDomain);
	}

	@Override
	@Transactional
	public MemberGradeMetric save(MemberGradeMetric metric) {
		MemberGradeMetricJpaEntity entity = memberGradeMetricRepository
				.findByMemberUuidAndMetricType(metric.memberUuid().value(), metric.metricType())
				.orElseGet(() -> GradePersistenceMapper.toEntity(metric));
		GradePersistenceMapper.applyToEntity(metric, entity);
		return GradePersistenceMapper.toDomain(memberGradeMetricRepository.save(entity));
	}
}
