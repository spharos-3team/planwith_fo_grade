package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.command.EvaluateGradeCommand;

public interface EvaluateGradeUseCase {

	void evaluate(EvaluateGradeCommand command);
}
