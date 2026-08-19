package com.planwith.planwith_fo_grade.application;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
import com.planwith.planwith_fo_grade.application.port.in.GrantGradeRewardUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeOutboxMessage;
import com.planwith.planwith_fo_grade.application.port.out.GradeRewardHistoryPort;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeRewardHistory;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_grade.domain.service.GradeBenefitSummary;

@Service
public class GrantGradeRewardService implements GrantGradeRewardUseCase {

	private static final Logger log = LoggerFactory.getLogger(GrantGradeRewardService.class);
	private static final String AGGREGATE_TYPE = "GradeRewardHistory";

	private final GradeMemberPort gradeMemberPort;
	private final GradeCriteriaPort gradeCriteriaPort;
	private final GradeRewardHistoryPort gradeRewardHistoryPort;
	private final GradeEventOutboxPort gradeEventOutboxPort;
	private final ObjectMapper objectMapper;

	public GrantGradeRewardService(
			GradeMemberPort gradeMemberPort,
			GradeCriteriaPort gradeCriteriaPort,
			GradeRewardHistoryPort gradeRewardHistoryPort,
			GradeEventOutboxPort gradeEventOutboxPort,
			ObjectMapper objectMapper
	) {
		this.gradeMemberPort = gradeMemberPort;
		this.gradeCriteriaPort = gradeCriteriaPort;
		this.gradeRewardHistoryPort = gradeRewardHistoryPort;
		this.gradeEventOutboxPort = gradeEventOutboxPort;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public void grant(GrantGradeRewardCommand command) {
		Objects.requireNonNull(command, "Grant grade reward command is required.");
		MemberUuid memberUuid = MemberUuid.from(command.memberUuid());
		String rewardType = resolveRewardType(command.rewardType());
		String rewardMonth = resolveRewardMonth(command.rewardMonth());

		log.info(
				"GrantGradeRewardService : grant : 월간 토큰 보상 지급 시작 - memberUuid={}, rewardMonth={}, rewardType={}",
				memberUuid,
				rewardMonth,
				rewardType
		);

		if (!GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN.equals(rewardType)) {
			log.warn(
					"GrantGradeRewardService : grant : 지원하지 않는 보상 타입이라 지급을 생략 - memberUuid={}, rewardType={}",
					memberUuid,
					rewardType
			);
			return;
		}

		GradeMember gradeMember = gradeMemberPort.findByMemberUuid(memberUuid).orElse(null);
		if (gradeMember == null) {
			log.warn("GrantGradeRewardService : grant : 등급이 없는 회원이라 지급을 생략 - memberUuid={}", memberUuid);
			return;
		}
		if (!gradeMember.isActive()) {
			log.warn(
					"GrantGradeRewardService : grant : ACTIVE 등급이 아니라 지급을 생략 - memberUuid={}, gradeStatus={}",
					memberUuid,
					gradeMember.gradeStatus()
			);
			return;
		}

		if (gradeRewardHistoryPort.existsByMemberUuidAndRewardMonth(memberUuid, rewardMonth)) {
			log.warn(
					"GrantGradeRewardService : grant : 동일 월 보상 이력이 있어 중복 지급을 생략 - memberUuid={}, rewardMonth={}",
					memberUuid,
					rewardMonth
			);
			return;
		}

		Grade currentGrade = requireCurrentGrade(gradeMember.gradeId());
		if (currentGrade == null) {
			log.warn(
					"GrantGradeRewardService : grant : 현재 등급 기준을 찾지 못해 지급을 생략 - memberUuid={}, gradeId={}",
					memberUuid,
					gradeMember.gradeId()
			);
			return;
		}

		long tokenAmount = GradeBenefitSummary.from(currentGrade.benefits()).monthlyTokenAmount();
		if (tokenAmount <= 0L) {
			log.warn(
					"GrantGradeRewardService : grant : 월간 토큰 혜택이 없어 지급을 생략 - memberUuid={}, gradeCode={}",
					memberUuid,
					currentGrade.gradeCode()
			);
			return;
		}

		LocalDateTime grantedAt = LocalDateTime.now(ZoneOffset.UTC);
		gradeRewardHistoryPort.save(GradeRewardHistory.create(
				memberUuid,
				currentGrade.gradeId(),
				rewardMonth,
				tokenAmount,
				grantedAt
		));

		String eventUuid = UUID.randomUUID().toString();
		gradeEventOutboxPort.save(new GradeOutboxMessage(
				eventUuid,
				AGGREGATE_TYPE,
				memberUuid.toString(),
				GradeRewardGrantedEvent.EVENT_TYPE,
				toPayload(GradeRewardGrantedEvent.of(
						eventUuid,
						memberUuid.toString(),
						currentGrade.gradeCode().name(),
						currentGrade.gradeLevel(),
						rewardMonth,
						tokenAmount,
						grantedAt.toInstant(ZoneOffset.UTC).toString()
				))
		));

		log.info(
				"GrantGradeRewardService : grant : 월간 토큰 보상 지급 및 GradeRewardGranted Outbox 저장 완료 - memberUuid={}, eventUuid={}, gradeCode={}, rewardMonth={}, tokenAmount={}",
				memberUuid,
				eventUuid,
				currentGrade.gradeCode(),
				rewardMonth,
				tokenAmount
		);
	}

	private Grade requireCurrentGrade(Long gradeId) {
		List<Grade> grades = gradeCriteriaPort.findAll();
		return grades.stream()
				.filter(grade -> grade.gradeId().equals(gradeId))
				.findFirst()
				.orElse(null);
	}

	private String toPayload(GradeRewardGrantedEvent event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			log.error("GrantGradeRewardService : toPayload : GradeRewardGranted 이벤트 직렬화 실패 - memberUuid={}",
					event.memberUuid());
			throw new IllegalStateException("Failed to serialize GradeRewardGranted event.", exception);
		}
	}

	private static String resolveRewardType(String rewardType) {
		if (rewardType == null || rewardType.isBlank()) {
			return GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN;
		}
		return rewardType.trim();
	}

	private static String resolveRewardMonth(String rewardMonth) {
		if (rewardMonth == null || rewardMonth.isBlank()) {
			return YearMonth.now(ZoneOffset.UTC).toString();
		}
		return rewardMonth.trim();
	}
}
