package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.query.GradeManagementPageView;

public interface GetGradeManagementPageQueryUseCase {

	GradeManagementPageView get(String memberUuid);
}
