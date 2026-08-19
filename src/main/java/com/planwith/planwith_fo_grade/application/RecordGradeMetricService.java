package com.planwith.planwith_fo_grade.application;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

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
import com.planwith.planwith_fo_grade.application.port.out.ProcessedGradeEventPort;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.ProcessedGradeEvent;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@Service
public class RecordGradeMetricService implements RecordGradeMetricUseCase {

	private static final Logger log = LoggerFactory.getLogger(RecordGradeMetricService.class);

	private final MemberGradeMetricPort memberGradeMetricPort;
	private final ProcessedGradeEventPort processedGradeEventPort;
	private final ObjectProvider<EvaluateGradeUseCase> evaluateGradeUseCase;

	public RecordGradeMetricService(
			MemberGradeMetricPort memberGradeMetricPort,
			ProcessedGradeEventPort processedGradeEventPort,
			ObjectProvider<EvaluateGradeUseCase> evaluateGradeUseCase
	) {
		this.memberGradeMetricPort = memberGradeMetricPort;
		this.processedGradeEventPort = processedGradeEventPort;
		this.evaluateGradeUseCase = evaluateGradeUseCase;
	}

	@Override
	@Transactional
	public void record(RecordGradeMetricCommand command) {
		Objects.requireNonNull(command, "Record grade metric command is required.");
		UUID eventUuid = parseEventUuid(command.eventUuid());
		MemberUuid memberUuid = MemberUuid.from(command.memberUuid());
		MemberMetricType metricType = parseMetricType(command.metricType());
		LocalDateTime synchronizedAt = LocalDateTime.now(ZoneOffset.UTC);

		if (processedGradeEventPort.existsByEventUuid(eventUuid)) {
			log.warn("RecordGradeMetricService : record : 중복 Metric 이벤트 무시 - eventUuid={}", eventUuid);
			return;
		}

		log.info(
				"RecordGradeMetricService : record : 회원 Metric 갱신 시작 - memberUuid={}, metricType={}, delta={}",
				memberUuid,
				metricType,
				command.delta()
		);

		processedGradeEventPort.save(ProcessedGradeEvent.recorded(
				eventUuid,
				memberUuid,
				metricType,
				synchronizedAt
		));

		MemberGradeMetric current = memberGradeMetricPort
				.findByMemberUuidAndMetricType(memberUuid, metricType)
				.orElseGet(() -> MemberGradeMetric.initialize(
						memberUuid,
						metricType,
						sourceService(metricType),
						synchronizedAt
				));
		long incomingVersion = command.sourceVersion() == null
				? current.sourceVersion() + 1
				: command.sourceVersion();
		if (incomingVersion <= current.sourceVersion()) {
			log.warn(
					"RecordGradeMetricService : record : 과거 Metric 이벤트 무시 - eventUuid={}, sourceVersion={}, currentVersion={}",
					eventUuid,
					incomingVersion,
					current.sourceVersion()
			);
			return;
		}

		long nextValue = Math.max(0L, current.currentValue() + command.delta());
		MemberGradeMetric updated = current.synchronize(
				nextValue,
				sourceService(metricType),
				incomingVersion,
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

	private static UUID parseEventUuid(String eventUuid) {
		if (eventUuid == null || eventUuid.isBlank()) {
			throw new IllegalArgumentException("Event UUID is required.");
		}
		try {
			return UUID.fromString(eventUuid.trim());
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Event UUID is invalid.");
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
