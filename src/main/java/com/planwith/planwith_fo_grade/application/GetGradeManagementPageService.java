package com.planwith.planwith_fo_grade.application;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.in.GetAllGradesQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.in.GetGradeManagementPageQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.in.GetMyGradeManagementQueryUseCase;
import com.planwith.planwith_fo_grade.application.query.GradeCatalogView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementPageView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;

@Service
public class GetGradeManagementPageService implements GetGradeManagementPageQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetGradeManagementPageService.class);

	private final GetAllGradesQueryUseCase getAllGradesQueryUseCase;
	private final GetMyGradeManagementQueryUseCase getMyGradeManagementQueryUseCase;

	public GetGradeManagementPageService(
			GetAllGradesQueryUseCase getAllGradesQueryUseCase,
			GetMyGradeManagementQueryUseCase getMyGradeManagementQueryUseCase
	) {
		this.getAllGradesQueryUseCase = getAllGradesQueryUseCase;
		this.getMyGradeManagementQueryUseCase = getMyGradeManagementQueryUseCase;
	}

	@Override
	@Transactional(readOnly = true)
	public GradeManagementPageView get(String memberUuidValue) {
		Objects.requireNonNull(memberUuidValue, "Member UUID is required.");
		log.info("GetGradeManagementPageService : get : 등급 관리 통합 조회 시작 - memberUuid={}", memberUuidValue);

		GradeManagementView member = getMyGradeManagementQueryUseCase.get(memberUuidValue);
		List<GradeCatalogView> grades = getAllGradesQueryUseCase.listAll();
		GradeManagementPageView view = new GradeManagementPageView(grades, member);

		log.info(
				"GetGradeManagementPageService : get : 등급 관리 통합 조회 완료 - memberUuid={}, gradeCount={}, currentGradeCode={}",
				memberUuidValue,
				grades.size(),
				member.currentGrade().code()
		);
		return view;
	}
}
