package com.planwith.planwith_fo_grade.adapter.in.web.dto;

import java.util.List;

import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.CurrentGradeResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.CurrentMetricsResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.NextGradeResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.ProgressResponse;

public record GradeManagementPageResponse(
		List<GradeResponse> grades,
		CurrentGradeResponse currentGrade,
		CurrentMetricsResponse currentMetrics,
		NextGradeResponse nextGrade,
		ProgressResponse progress,
		CurrentBenefitSummaryResponse currentBenefits
) {
}
