package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.command.GrantMonthlyGradeRewardsCommand;

public interface GrantMonthlyGradeRewardsUseCase {

	void grantAll(GrantMonthlyGradeRewardsCommand command);
}
