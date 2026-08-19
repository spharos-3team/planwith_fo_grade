package com.planwith.planwith_fo_grade.application.query;

import java.util.List;

public record GradeManagementPageView(
		List<GradeCatalogView> grades,
		GradeManagementView member
) {
}
