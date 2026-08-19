package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@Component
public class GradeMemberPersistenceAdapter implements GradeMemberPort {

	private final SpringDataGradeMemberRepository gradeMemberRepository;

	public GradeMemberPersistenceAdapter(SpringDataGradeMemberRepository gradeMemberRepository) {
		this.gradeMemberRepository = gradeMemberRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<GradeMember> findByMemberUuid(MemberUuid memberUuid) {
		return gradeMemberRepository.findById(memberUuid.value())
				.map(GradePersistenceMapper::toDomain);
	}

	@Override
	@Transactional
	public GradeMember save(GradeMember member) {
		GradeMemberJpaEntity entity = gradeMemberRepository.findById(member.memberUuid().value())
				.orElseGet(() -> GradeMemberJpaEntity.createNew(member.memberUuid().value()));
		GradePersistenceMapper.applyToEntity(member, entity);
		return GradePersistenceMapper.toDomain(gradeMemberRepository.save(entity));
	}
}
