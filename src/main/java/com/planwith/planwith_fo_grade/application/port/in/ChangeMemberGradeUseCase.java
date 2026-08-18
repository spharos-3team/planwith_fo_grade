package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.command.ChangeMemberGradeCommand;

public interface ChangeMemberGradeUseCase {

	void change(ChangeMemberGradeCommand command);
}
