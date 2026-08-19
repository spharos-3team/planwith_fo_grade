package com.planwith.planwith_fo_grade.application.query;

import java.util.List;

public record GradeCatalogView(
		String gradeCode,
		String gradeName,
		int gradeLevel,
		List<GradeConditionView> conditions,
		List<GradeBenefitView> benefits
) {
}
