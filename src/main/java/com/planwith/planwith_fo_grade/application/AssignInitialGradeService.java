package com.planwith.planwith_fo_grade.application;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.command.AssignInitialGradeCommand;
import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
import com.planwith.planwith_fo_grade.application.port.in.AssignInitialGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.in.GrantGradeRewardUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@Service
public class AssignInitialGradeService implements AssignInitialGradeUseCase {

	private static final Logger log = LoggerFactory.getLogger(AssignInitialGradeService.class);

	private final GradeCriteriaPort gradeCriteriaPort;
	private final GradeMemberPort gradeMemberPort;
	private final GrantGradeRewardUseCase grantGradeRewardUseCase;

	public AssignInitialGradeService(
			GradeCriteriaPort gradeCriteriaPort,
			GradeMemberPort gradeMemberPort,
			GrantGradeRewardUseCase grantGradeRewardUseCase
	) {
		this.gradeCriteriaPort = gradeCriteriaPort;
		this.gradeMemberPort = gradeMemberPort;
		this.grantGradeRewardUseCase = grantGradeRewardUseCase;
	}

	@Override
	@Transactional
	public void assign(AssignInitialGradeCommand command) {
		Objects.requireNonNull(command, "Assign initial grade command is required.");
		MemberUuid memberUuid = MemberUuid.from(command.memberUuid());
		LocalDateTime assignedAt = Objects.requireNonNull(command.assignedAt(), "Assigned at is required.");

		log.info("AssignInitialGradeService : assign : 회원 초기 등급 부여 시작 - memberUuid={}", memberUuid);

		if (gradeMemberPort.findByMemberUuid(memberUuid).isPresent()) {
			log.warn("AssignInitialGradeService : assign : 이미 등급이 부여된 회원이라 초기 등급 부여를 생략 - memberUuid={}",
					memberUuid);
			grantSignupMonthToken(memberUuid, assignedAt);
			return;
		}

		Grade initialGrade = gradeCriteriaPort.findLowestGrade()
				.orElseThrow(() -> new InvalidGradeException("Lowest grade criteria is not configured."));

		GradeMember saved = gradeMemberPort.save(
				GradeMember.assign(initialGrade.gradeId(), memberUuid, assignedAt)
		);
		grantSignupMonthToken(saved.memberUuid(), assignedAt);

		log.info(
				"AssignInitialGradeService : assign : 회원 초기 등급 및 가입월 토큰 지급 요청 완료 - memberUuid={}, gradeCode={}, gradeId={}",
				saved.memberUuid(),
				initialGrade.gradeCode(),
				saved.gradeId()
		);
	}

	private void grantSignupMonthToken(MemberUuid memberUuid, LocalDateTime assignedAt) {
		String rewardMonth = YearMonth.from(assignedAt).toString();
		log.info(
				"AssignInitialGradeService : grantSignupMonthToken : 가입월 토큰 지급 요청 - memberUuid={}, rewardMonth={}",
				memberUuid,
				rewardMonth
		);
		grantGradeRewardUseCase.grant(new GrantGradeRewardCommand(
				memberUuid.toString(),
				null,
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				rewardMonth
		));
	}
}
