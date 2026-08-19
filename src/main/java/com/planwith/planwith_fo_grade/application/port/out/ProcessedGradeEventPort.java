package com.planwith.planwith_fo_grade.application.port.out;

import java.util.UUID;

import com.planwith.planwith_fo_grade.domain.model.ProcessedGradeEvent;

public interface ProcessedGradeEventPort {

	boolean existsByEventUuid(UUID eventUuid);

	void save(ProcessedGradeEvent event);
}
