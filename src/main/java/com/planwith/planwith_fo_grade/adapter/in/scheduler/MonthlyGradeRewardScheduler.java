package com.planwith.planwith_fo_grade.adapter.in.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_grade.application.command.GrantMonthlyGradeRewardsCommand;
import com.planwith.planwith_fo_grade.application.port.in.GrantMonthlyGradeRewardsUseCase;

@Component
@ConditionalOnProperty(name = "grade.reward.monthly-enabled", havingValue = "true")
public class MonthlyGradeRewardScheduler {

	private static final Logger log = LoggerFactory.getLogger(MonthlyGradeRewardScheduler.class);

	private final GrantMonthlyGradeRewardsUseCase grantMonthlyGradeRewardsUseCase;

	public MonthlyGradeRewardScheduler(GrantMonthlyGradeRewardsUseCase grantMonthlyGradeRewardsUseCase) {
		this.grantMonthlyGradeRewardsUseCase = grantMonthlyGradeRewardsUseCase;
	}

	@Scheduled(cron = "${grade.reward.monthly-cron:0 0 0 1 * *}", zone = "UTC")
	public void grantMonthlyRewards() {
		log.info("MonthlyGradeRewardScheduler : grantMonthlyRewards : 월간 토큰 보상 스케줄 시작");
		grantMonthlyGradeRewardsUseCase.grantAll(new GrantMonthlyGradeRewardsCommand(null));
		log.info("MonthlyGradeRewardScheduler : grantMonthlyRewards : 월간 토큰 보상 스케줄 완료");
	}
}
