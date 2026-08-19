package com.planwith.planwith_fo_grade.adapter.in.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_grade.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeBenefitResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeConditionResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeResponse;
import com.planwith.planwith_fo_grade.application.port.in.GetAllGradesQueryUseCase;
import com.planwith.planwith_fo_grade.application.query.GradeBenefitView;
import com.planwith.planwith_fo_grade.application.query.GradeCatalogView;
import com.planwith.planwith_fo_grade.application.query.GradeConditionView;

@RestController
@RequestMapping("/api/grade")
public class GradeQueryController {

	private static final Logger log = LoggerFactory.getLogger(GradeQueryController.class);

	private final GetAllGradesQueryUseCase getAllGradesQueryUseCase;

	public GradeQueryController(GetAllGradesQueryUseCase getAllGradesQueryUseCase) {
		this.getAllGradesQueryUseCase = getAllGradesQueryUseCase;
	}

	// 전체 등급표 조회
	@GetMapping("/grades")
	public ResponseEntity<ApiResponse<List<GradeResponse>>> listGrades() {
		log.info("GradeQueryController : GET listGrades : 전체 등급표 조회 요청");
		List<GradeResponse> response = getAllGradesQueryUseCase.listAll().stream()
				.map(GradeQueryController::toResponse)
				.toList();
		log.info("GradeQueryController : GET listGrades : 전체 등급표 조회 완료 - gradeCount={}", response.size());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	private static GradeResponse toResponse(GradeCatalogView view) {
		return new GradeResponse(
				view.gradeCode(),
				view.gradeName(),
				view.gradeLevel(),
				view.conditions().stream().map(GradeQueryController::toConditionResponse).toList(),
				view.benefits().stream().map(GradeQueryController::toBenefitResponse).toList()
		);
	}

	private static GradeConditionResponse toConditionResponse(GradeConditionView view) {
		return new GradeConditionResponse(
				view.metricType(),
				view.conditionName(),
				view.thresholdValue(),
				view.description()
		);
	}

	private static GradeBenefitResponse toBenefitResponse(GradeBenefitView view) {
		return new GradeBenefitResponse(
				view.benefitCode(),
				view.benefitName(),
				view.benefitValue(),
				view.description()
		);
	}
}
