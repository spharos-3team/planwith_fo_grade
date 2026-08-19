package com.planwith.planwith_fo_grade.application;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;
import com.planwith.planwith_fo_grade.application.command.GrantMonthlyGradeRewardsCommand;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
import com.planwith.planwith_fo_grade.application.port.in.GrantGradeRewardUseCase;
import com.planwith.planwith_fo_grade.application.port.in.GrantMonthlyGradeRewardsUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;

@Service
public class GrantMonthlyGradeRewardsService implements GrantMonthlyGradeRewardsUseCase {

	private static final Logger log = LoggerFactory.getLogger(GrantMonthlyGradeRewardsService.class);

	private final GradeMemberPort gradeMemberPort;
	private final GrantGradeRewardUseCase grantGradeRewardUseCase;

	public GrantMonthlyGradeRewardsService(
			GradeMemberPort gradeMemberPort,
			GrantGradeRewardUseCase grantGradeRewardUseCase
	) {
		this.gradeMemberPort = gradeMemberPort;
		this.grantGradeRewardUseCase = grantGradeRewardUseCase;
	}

	@Override
	public void grantAll(GrantMonthlyGradeRewardsCommand command) {
		Objects.requireNonNull(command, "Grant monthly grade rewards command is required.");
		String rewardMonth = resolveRewardMonth(command.rewardMonth());
		List<GradeMember> activeMembers = gradeMemberPort.findAllActive();
		log.info(
				"GrantMonthlyGradeRewardsService : grantAll : 월간 토큰 보상 대상 계산 시작 - rewardMonth={}, activeMemberCount={}",
				rewardMonth,
				activeMembers.size()
		);

		int grantedCount = 0;
		int skippedCount = 0;
		for (GradeMember member : activeMembers) {
			try {
				grantGradeRewardUseCase.grant(new GrantGradeRewardCommand(
						member.memberUuid().toString(),
						null,
						GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
						rewardMonth
				));
				grantedCount++;
			} catch (RuntimeException exception) {
				skippedCount++;
				log.warn(
						"GrantMonthlyGradeRewardsService : grantAll : 회원 월간 토큰 보상 지급 실패 - memberUuid={}, rewardMonth={}",
						member.memberUuid(),
						rewardMonth,
						exception
				);
			}
		}

		log.info(
				"GrantMonthlyGradeRewardsService : grantAll : 월간 토큰 보상 대상 계산 완료 - rewardMonth={}, requestedCount={}, failureCount={}",
				rewardMonth,
				grantedCount,
				skippedCount
		);
	}

	private static String resolveRewardMonth(String rewardMonth) {
		if (rewardMonth == null || rewardMonth.isBlank()) {
			return YearMonth.now(ZoneOffset.UTC).toString();
		}
		return rewardMonth.trim();
	}
}
