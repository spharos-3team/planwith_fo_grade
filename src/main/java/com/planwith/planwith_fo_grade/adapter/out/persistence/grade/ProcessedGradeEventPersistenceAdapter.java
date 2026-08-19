package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.out.ProcessedGradeEventPort;
import com.planwith.planwith_fo_grade.domain.model.ProcessedGradeEvent;

@Component
public class ProcessedGradeEventPersistenceAdapter implements ProcessedGradeEventPort {

	private final SpringDataProcessedGradeEventRepository processedGradeEventRepository;

	public ProcessedGradeEventPersistenceAdapter(
			SpringDataProcessedGradeEventRepository processedGradeEventRepository
	) {
		this.processedGradeEventRepository = processedGradeEventRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEventUuid(UUID eventUuid) {
		return processedGradeEventRepository.existsByEventUuid(eventUuid);
	}

	@Override
	@Transactional
	public void save(ProcessedGradeEvent event) {
		processedGradeEventRepository.save(ProcessedGradeEventJpaEntity.create(
				event.eventUuid(),
				event.memberUuid().value(),
				event.metricType(),
				event.processedAt()
		));
	}
}
