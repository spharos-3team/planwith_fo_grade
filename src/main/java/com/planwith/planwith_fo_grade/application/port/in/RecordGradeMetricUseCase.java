package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;

public interface RecordGradeMetricUseCase {

	void record(RecordGradeMetricCommand command);
}
