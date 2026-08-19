package com.planwith.planwith_fo_grade.adapter.in.web.dto;

import java.util.List;

public record GradeResponse(
		String gradeCode,
		String gradeName,
		int gradeLevel,
		List<GradeConditionResponse> conditions,
		List<GradeBenefitResponse> benefits
) {
}
