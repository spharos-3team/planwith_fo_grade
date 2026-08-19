package com.planwith.planwith_fo_grade.application;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.in.GetCurrentBenefitsQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.query.CurrentBenefitSummaryView;
import com.planwith.planwith_fo_grade.domain.exception.GradeNotFoundException;
import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@Service
public class GetCurrentBenefitsService implements GetCurrentBenefitsQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetCurrentBenefitsService.class);

	private final GradeMemberPort gradeMemberPort;
	private final GradeCriteriaPort gradeCriteriaPort;

	public GetCurrentBenefitsService(
			GradeMemberPort gradeMemberPort,
			GradeCriteriaPort gradeCriteriaPort
	) {
		this.gradeMemberPort = gradeMemberPort;
		this.gradeCriteriaPort = gradeCriteriaPort;
	}

	@Override
	@Transactional(readOnly = true)
	public CurrentBenefitSummaryView get(String memberUuidValue) {
		Objects.requireNonNull(memberUuidValue, "Member UUID is required.");
		MemberUuid memberUuid = MemberUuid.from(memberUuidValue);
		log.info("GetCurrentBenefitsService : get : 현재 혜택 조회 시작 - memberUuid={}", memberUuid);

		GradeMember gradeMember = gradeMemberPort.findByMemberUuid(memberUuid)
				.orElseThrow(() -> new GradeNotFoundException(memberUuid.toString()));
		Grade currentGrade = requireCurrentGrade(gradeMember.gradeId());
		CurrentBenefitSummaryView view = CurrentBenefitSummaryView.from(currentGrade);

		log.info(
				"GetCurrentBenefitsService : get : 현재 혜택 조회 완료 - memberUuid={}, gradeCode={}, monthlyTokenAmount={}",
				memberUuid,
				view.gradeCode(),
				view.monthlyTokenAmount()
		);
		return view;
	}

	private Grade requireCurrentGrade(Long gradeId) {
		List<Grade> grades = gradeCriteriaPort.findAll();
		return grades.stream()
				.filter(grade -> grade.gradeId().equals(gradeId))
				.findFirst()
				.orElseThrow(() -> new InvalidGradeException("Grade criteria is not configured for the member."));
	}
}
