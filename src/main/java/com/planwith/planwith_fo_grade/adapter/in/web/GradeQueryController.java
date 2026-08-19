package com.planwith.planwith_fo_grade.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_grade.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.CurrentBenefitSummaryResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeBenefitResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeConditionResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.CurrentGradeResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.CurrentMetricsResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.MetricProgressResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.NextGradeResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeManagementResponse.ProgressResponse;
import com.planwith.planwith_fo_grade.adapter.in.web.dto.GradeResponse;
import com.planwith.planwith_fo_grade.application.port.in.GetAllGradesQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.in.GetCurrentBenefitsQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.in.GetMyGradeManagementQueryUseCase;
import com.planwith.planwith_fo_grade.application.query.CurrentBenefitSummaryView;
import com.planwith.planwith_fo_grade.application.query.GradeBenefitView;
import com.planwith.planwith_fo_grade.application.query.GradeCatalogView;
import com.planwith.planwith_fo_grade.application.query.GradeConditionView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.MetricProgressView;

@RestController
@RequestMapping("/api/grade")
public class GradeQueryController {

	private static final Logger log = LoggerFactory.getLogger(GradeQueryController.class);

	private final GetAllGradesQueryUseCase getAllGradesQueryUseCase;
	private final GetMyGradeManagementQueryUseCase getMyGradeManagementQueryUseCase;
	private final GetCurrentBenefitsQueryUseCase getCurrentBenefitsQueryUseCase;

	public GradeQueryController(
			GetAllGradesQueryUseCase getAllGradesQueryUseCase,
			GetMyGradeManagementQueryUseCase getMyGradeManagementQueryUseCase,
			GetCurrentBenefitsQueryUseCase getCurrentBenefitsQueryUseCase
	) {
		this.getAllGradesQueryUseCase = getAllGradesQueryUseCase;
		this.getMyGradeManagementQueryUseCase = getMyGradeManagementQueryUseCase;
		this.getCurrentBenefitsQueryUseCase = getCurrentBenefitsQueryUseCase;
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

	// 내 등급 관리 조회
	@GetMapping("/grades/me")
	public ResponseEntity<ApiResponse<GradeManagementResponse>> getMyGradeManagement(
			@RequestHeader("X-Member-UUID") UUID memberUuid
	) {
		log.info("GradeQueryController : GET getMyGradeManagement : 내 등급 관리 조회 요청 - memberUuid={}", memberUuid);
		GradeManagementResponse response = toManagementResponse(getMyGradeManagementQueryUseCase.get(memberUuid.toString()));
		log.info("GradeQueryController : GET getMyGradeManagement : 내 등급 관리 조회 완료 - memberUuid={}", memberUuid);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 현재 혜택 조회
	@GetMapping("/grades/me/benefits")
	public ResponseEntity<ApiResponse<CurrentBenefitSummaryResponse>> getMyCurrentBenefits(
			@RequestHeader("X-Member-UUID") UUID memberUuid
	) {
		log.info("GradeQueryController : GET getMyCurrentBenefits : 현재 혜택 조회 요청 - memberUuid={}", memberUuid);
		CurrentBenefitSummaryResponse response = toBenefitSummaryResponse(
				getCurrentBenefitsQueryUseCase.get(memberUuid.toString())
		);
		log.info("GradeQueryController : GET getMyCurrentBenefits : 현재 혜택 조회 완료 - memberUuid={}", memberUuid);
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

	private static GradeManagementResponse toManagementResponse(GradeManagementView view) {
		return new GradeManagementResponse(
				new CurrentGradeResponse(
						view.currentGrade().code(),
						view.currentGrade().name(),
						view.currentGrade().level(),
						view.currentGrade().benefits().stream().map(GradeQueryController::toBenefitResponse).toList()
				),
				new CurrentMetricsResponse(
						view.currentMetrics().storyCount(),
						view.currentMetrics().followerCount(),
						view.currentMetrics().receivedLikeCount()
				),
				view.nextGrade() == null ? null : new NextGradeResponse(
						view.nextGrade().code(),
						view.nextGrade().name(),
						view.nextGrade().conditions().stream().map(GradeQueryController::toConditionResponse).toList()
				),
				new ProgressResponse(
						toMetricProgressResponse(view.progress().story()),
						toMetricProgressResponse(view.progress().follower()),
						toMetricProgressResponse(view.progress().receivedLike())
				),
				toBenefitSummaryResponse(view.currentBenefits())
		);
	}

	private static CurrentBenefitSummaryResponse toBenefitSummaryResponse(CurrentBenefitSummaryView view) {
		return new CurrentBenefitSummaryResponse(
				view.gradeCode(),
				view.gradeName(),
				view.gradeLevel(),
				view.monthlyTokenAmount(),
				view.profileBadge(),
				view.profileSpecialBorder(),
				view.membershipPublicStory(),
				view.membershipAccess(),
				view.storyPriorityExposure()
		);
	}

	private static MetricProgressResponse toMetricProgressResponse(MetricProgressView view) {
		return new MetricProgressResponse(
				view.current(),
				view.required(),
				view.remaining(),
				view.percentage()
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
