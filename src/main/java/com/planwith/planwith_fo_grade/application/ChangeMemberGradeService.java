package com.planwith.planwith_fo_grade.application;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.command.ChangeMemberGradeCommand;
import com.planwith.planwith_fo_grade.application.event.GradeChangedEvent;
import com.planwith.planwith_fo_grade.application.port.in.ChangeMemberGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeOutboxMessage;
import com.planwith.planwith_fo_grade.application.port.out.GradeQueryCachePort;
import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_grade.domain.service.GradeBenefitSummary;

@Service
public class ChangeMemberGradeService implements ChangeMemberGradeUseCase {

	private static final Logger log = LoggerFactory.getLogger(ChangeMemberGradeService.class);
	private static final String AGGREGATE_TYPE = "GradeMember";

	private final GradeMemberPort gradeMemberPort;
	private final GradeCriteriaPort gradeCriteriaPort;
	private final GradeEventOutboxPort gradeEventOutboxPort;
	private final GradeQueryCachePort gradeQueryCachePort;
	private final ObjectMapper objectMapper;

	public ChangeMemberGradeService(
			GradeMemberPort gradeMemberPort,
			GradeCriteriaPort gradeCriteriaPort,
			GradeEventOutboxPort gradeEventOutboxPort,
			GradeQueryCachePort gradeQueryCachePort,
			ObjectMapper objectMapper
	) {
		this.gradeMemberPort = gradeMemberPort;
		this.gradeCriteriaPort = gradeCriteriaPort;
		this.gradeEventOutboxPort = gradeEventOutboxPort;
		this.gradeQueryCachePort = gradeQueryCachePort;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public void change(ChangeMemberGradeCommand command) {
		Objects.requireNonNull(command, "Change member grade command is required.");
		MemberUuid memberUuid = MemberUuid.from(command.memberUuid());
		GradeCode fromGradeCode = parseGradeCode(command.fromGradeCode());
		GradeCode toGradeCode = parseGradeCode(command.toGradeCode());
		LocalDateTime changedAt = LocalDateTime.now(ZoneOffset.UTC);

		log.info(
				"ChangeMemberGradeService : change : 등급 변경 시작 - memberUuid={}, fromGradeCode={}, toGradeCode={}",
				memberUuid,
				fromGradeCode,
				toGradeCode
		);

		GradeMember gradeMember = gradeMemberPort.findByMemberUuid(memberUuid)
				.orElseThrow(() -> new InvalidGradeException("Member grade is not assigned."));
		if (!gradeMember.isActive()) {
			throw new InvalidGradeException("Only active members can change grade.");
		}

		Grade currentGrade = requireGrade(fromGradeCode);
		if (!currentGrade.gradeId().equals(gradeMember.gradeId())) {
			throw new InvalidGradeException("Current grade does not match the requested from grade.");
		}

		Grade targetGrade = requireGrade(toGradeCode);
		if (targetGrade.gradeLevel() <= currentGrade.gradeLevel()) {
			throw new InvalidGradeException("Grade change allows promotion only.");
		}

		GradeMember saved = gradeMemberPort.save(gradeMember.changeGrade(targetGrade.gradeId(), changedAt));
		String eventUuid = UUID.randomUUID().toString();
		gradeEventOutboxPort.save(new GradeOutboxMessage(
				eventUuid,
				AGGREGATE_TYPE,
				saved.memberUuid().toString(),
				GradeChangedEvent.EVENT_TYPE,
				toPayload(GradeChangedEvent.of(
						eventUuid,
						saved.memberUuid().toString(),
						currentGrade.gradeCode().name(),
						targetGrade.gradeCode().name(),
						currentGrade.gradeLevel(),
						targetGrade.gradeLevel(),
						changedAt.toInstant(ZoneOffset.UTC).toString(),
						GradeChangedEvent.CurrentBenefits.from(GradeBenefitSummary.from(targetGrade.benefits()))
				))
		));

		log.info(
				"ChangeMemberGradeService : change : 등급 변경 및 GradeChanged Outbox 저장 완료 - memberUuid={}, eventUuid={}, fromGradeCode={}, toGradeCode={}",
				saved.memberUuid(),
				eventUuid,
				currentGrade.gradeCode(),
				targetGrade.gradeCode()
		);
		gradeQueryCachePort.evict(saved.memberUuid().toString());
	}

	private Grade requireGrade(GradeCode gradeCode) {
		return gradeCriteriaPort.findByGradeCode(gradeCode)
				.orElseThrow(() -> new InvalidGradeException("Grade criteria is not configured: " + gradeCode));
	}

	private String toPayload(GradeChangedEvent event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			log.error("ChangeMemberGradeService : toPayload : GradeChanged 이벤트 직렬화 실패 - memberUuid={}",
					event.memberUuid());
			throw new IllegalStateException("Failed to serialize GradeChanged event.", exception);
		}
	}

	private static GradeCode parseGradeCode(String value) {
		if (value == null || value.isBlank()) {
			throw new InvalidGradeException("Grade code is required.");
		}
		try {
			return GradeCode.valueOf(value);
		} catch (IllegalArgumentException exception) {
			throw new InvalidGradeException("Unknown grade code: " + value);
		}
	}
}
