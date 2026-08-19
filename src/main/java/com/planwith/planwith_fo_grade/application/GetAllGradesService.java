package com.planwith.planwith_fo_grade.application;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.in.GetAllGradesQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.query.GradeBenefitView;
import com.planwith.planwith_fo_grade.application.query.GradeCatalogView;
import com.planwith.planwith_fo_grade.application.query.GradeConditionView;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeBenefit;
import com.planwith.planwith_fo_grade.domain.model.GradeCondition;

@Service
public class GetAllGradesService implements GetAllGradesQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetAllGradesService.class);

	private final GradeCriteriaPort gradeCriteriaPort;

	public GetAllGradesService(GradeCriteriaPort gradeCriteriaPort) {
		this.gradeCriteriaPort = gradeCriteriaPort;
	}

	@Override
	@Transactional(readOnly = true)
	public List<GradeCatalogView> listAll() {
		log.info("GetAllGradesService : listAll : 전체 등급표 조회 시작");
		List<GradeCatalogView> grades = gradeCriteriaPort.findAll().stream()
				.map(GetAllGradesService::toView)
				.toList();
		log.info("GetAllGradesService : listAll : 전체 등급표 조회 완료 - gradeCount={}", grades.size());
		return grades;
	}

	private static GradeCatalogView toView(Grade grade) {
		return new GradeCatalogView(
				grade.gradeCode().name(),
				grade.gradeName(),
				grade.gradeLevel(),
				grade.conditions().stream().map(GetAllGradesService::toConditionView).toList(),
				grade.benefits().stream().map(GetAllGradesService::toBenefitView).toList()
		);
	}

	private static GradeConditionView toConditionView(GradeCondition condition) {
		return new GradeConditionView(
				condition.metricType().name(),
				condition.conditionName(),
				condition.thresholdValue(),
				condition.description()
		);
	}

	private static GradeBenefitView toBenefitView(GradeBenefit benefit) {
		return new GradeBenefitView(
				benefit.benefitCode().name(),
				benefit.benefitName(),
				benefit.benefitValue(),
				benefit.description()
		);
	}
}
