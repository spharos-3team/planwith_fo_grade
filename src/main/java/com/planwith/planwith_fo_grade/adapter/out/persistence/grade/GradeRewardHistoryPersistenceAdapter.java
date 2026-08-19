package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.out.GradeRewardHistoryPort;
import com.planwith.planwith_fo_grade.domain.model.GradeRewardHistory;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@Component
public class GradeRewardHistoryPersistenceAdapter implements GradeRewardHistoryPort {

	private final SpringDataGradeRewardHistoryRepository repository;

	public GradeRewardHistoryPersistenceAdapter(SpringDataGradeRewardHistoryRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByMemberUuidAndRewardMonth(MemberUuid memberUuid, String rewardMonth) {
		return repository.existsByMemberUuidAndRewardMonth(memberUuid.value(), rewardMonth);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<GradeRewardHistory> findByMemberUuidAndRewardMonth(MemberUuid memberUuid, String rewardMonth) {
		return repository.findByMemberUuidAndRewardMonth(memberUuid.value(), rewardMonth)
				.map(GradePersistenceMapper::toDomain);
	}

	@Override
	@Transactional
	public GradeRewardHistory save(GradeRewardHistory history) {
		GradeRewardHistoryJpaEntity entity;
		if (history.rewardId() == null) {
			entity = GradePersistenceMapper.toEntity(history);
		} else {
			entity = repository.findById(history.rewardId())
					.orElseGet(() -> GradePersistenceMapper.toEntity(history));
			GradePersistenceMapper.applyToEntity(history, entity);
		}
		return GradePersistenceMapper.toDomain(repository.save(entity));
	}
}
