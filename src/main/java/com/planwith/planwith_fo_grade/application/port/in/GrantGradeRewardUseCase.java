package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;

public interface GrantGradeRewardUseCase {

	void grant(GrantGradeRewardCommand command);
}
