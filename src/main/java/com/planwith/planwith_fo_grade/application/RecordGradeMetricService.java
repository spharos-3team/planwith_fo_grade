package com.planwith.planwith_fo_grade.application;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.command.EvaluateGradeCommand;
import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;
import com.planwith.planwith_fo_grade.application.port.in.EvaluateGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.in.RecordGradeMetricUseCase;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@Service
public class RecordGradeMetricService implements RecordGradeMetricUseCase {

	private static final Logger log = LoggerFactory.getLogger(RecordGradeMetricService.class);

	private final MemberGradeMetricPort memberGradeMetricPort;
	private final ObjectProvider<EvaluateGradeUseCase> evaluateGradeUseCase;

	public RecordGradeMetricService(
			MemberGradeMetricPort memberGradeMetricPort,
			ObjectProvider<EvaluateGradeUseCase> evaluateGradeUseCase
	) {
		this.memberGradeMetricPort = memberGradeMetricPort;
		this.evaluateGradeUseCase = evaluateGradeUseCase;
	}

	@Override
	@Transactional
	public void record(RecordGradeMetricCommand command) {
		Objects.requireNonNull(command, "Record grade metric command is required.");
		MemberUuid memberUuid = MemberUuid.from(command.memberUuid());
		MemberMetricType metricType = parseMetricType(command.metricType());
		LocalDateTime synchronizedAt = LocalDateTime.now(ZoneOffset.UTC);

		log.info(
				"RecordGradeMetricService : record : 회원 Metric 갱신 시작 - memberUuid={}, metricType={}, delta={}",
				memberUuid,
				metricType,
				command.delta()
		);

		MemberGradeMetric current = memberGradeMetricPort
				.findByMemberUuidAndMetricType(memberUuid, metricType)
				.orElseGet(() -> MemberGradeMetric.initialize(
						memberUuid,
						metricType,
						sourceService(metricType),
						synchronizedAt
				));
		long nextValue = Math.max(0L, current.currentValue() + command.delta());
		MemberGradeMetric updated = current.synchronize(
				nextValue,
				sourceService(metricType),
				current.sourceVersion() + 1,
				synchronizedAt
		);
		MemberGradeMetric saved = memberGradeMetricPort.save(updated);

		log.info(
				"RecordGradeMetricService : record : 회원 Metric 갱신 완료 - memberUuid={}, metricType={}, currentValue={}",
				saved.memberUuid(),
				saved.metricType(),
				saved.currentValue()
		);
		triggerGradeEvaluation(memberUuid);
	}

	private void triggerGradeEvaluation(MemberUuid memberUuid) {
		EvaluateGradeUseCase useCase = evaluateGradeUseCase.getIfAvailable();
		if (useCase == null) {
			log.debug("RecordGradeMetricService : record : 등급 평가 UseCase가 없어 재평가를 생략 - memberUuid={}",
					memberUuid);
			return;
		}
		try {
			useCase.evaluate(new EvaluateGradeCommand(memberUuid.toString()));
			log.info("RecordGradeMetricService : record : 등급 재평가 트리거 완료 - memberUuid={}", memberUuid);
		} catch (RuntimeException exception) {
			log.warn("RecordGradeMetricService : record : 등급 재평가 트리거 실패 - memberUuid={}", memberUuid);
		}
	}

	private static MemberMetricType parseMetricType(String metricType) {
		if (metricType == null || metricType.isBlank()) {
			throw new IllegalArgumentException("Metric type is required.");
		}
		try {
			return MemberMetricType.valueOf(metricType.trim());
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Unsupported metric type: " + metricType);
		}
	}

	private static String sourceService(MemberMetricType metricType) {
		return switch (metricType) {
			case STORY_COUNT, POST_COUNT -> "story-service";
			case FOLLOWER_COUNT -> "follow-service";
			case RECEIVED_LIKE_COUNT -> "like-service";
		};
	}
}
