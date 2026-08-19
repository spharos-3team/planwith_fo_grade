package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.query.GradeManagementView;

public interface GetMyGradeManagementQueryUseCase {

	GradeManagementView get(String memberUuid);
}
