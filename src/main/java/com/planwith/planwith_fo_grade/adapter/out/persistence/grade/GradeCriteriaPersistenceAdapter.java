package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;

@Component
public class GradeCriteriaPersistenceAdapter implements GradeCriteriaPort {

	private final SpringDataGradeRepository gradeRepository;

	public GradeCriteriaPersistenceAdapter(SpringDataGradeRepository gradeRepository) {
		this.gradeRepository = gradeRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Grade> findAll() {
		List<GradeJpaEntity> entities = gradeRepository.findAllByOrderByGradeLevelAsc();
		entities.forEach(GradeCriteriaPersistenceAdapter::initializeCriteria);
		return entities.stream()
				.map(GradePersistenceMapper::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Grade> findByGradeCode(GradeCode gradeCode) {
		return gradeRepository.findByGradeCode(gradeCode)
				.map(entity -> {
					initializeCriteria(entity);
					return GradePersistenceMapper.toDomain(entity);
				});
	}

	@Override
	@Transactional
	public Grade save(Grade grade) {
		GradeJpaEntity saved = gradeRepository.save(GradePersistenceMapper.toEntity(grade));
		initializeCriteria(saved);
		return GradePersistenceMapper.toDomain(saved);
	}

	private static void initializeCriteria(GradeJpaEntity entity) {
		entity.getConditions();
		entity.getBenefits();
	}
}
