package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.command.AssignInitialGradeCommand;

public interface AssignInitialGradeUseCase {

	void assign(AssignInitialGradeCommand command);
}
