package com.planwith.planwith_fo_grade.adapter.in.kafka;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.MemberCreatedEventPayload;
import com.planwith.planwith_fo_grade.application.command.AssignInitialGradeCommand;
import com.planwith.planwith_fo_grade.application.port.in.AssignInitialGradeUseCase;

@Component
@ConditionalOnProperty(name = "grade.kafka.consumer-enabled", havingValue = "true")
public class MemberCreatedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(MemberCreatedEventConsumer.class);

	private final AssignInitialGradeUseCase assignInitialGradeUseCase;
	private final ObjectMapper objectMapper;

	public MemberCreatedEventConsumer(
			AssignInitialGradeUseCase assignInitialGradeUseCase,
			ObjectMapper objectMapper
	) {
		this.assignInitialGradeUseCase = assignInitialGradeUseCase;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "${grade.kafka.topics.member-created}")
	public void consume(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		log.info("MemberCreatedEventConsumer : consume : 회원 생성 이벤트 수신 - topic={}", topic);
		MemberCreatedEventPayload event = parse(payload);
		if (event == null || event.memberUuid() == null || event.memberUuid().isBlank()) {
			log.error("MemberCreatedEventConsumer : consume : 회원 생성 이벤트 파싱 실패로 초기 등급 부여를 생략");
			return;
		}
		log.debug("MemberCreatedEventConsumer : consume : 회원 생성 이벤트 확인 - memberUuid={}", event.memberUuid());
		try {
			assignInitialGradeUseCase.assign(new AssignInitialGradeCommand(
					event.memberUuid(),
					LocalDateTime.now(ZoneOffset.UTC)
			));
		} catch (IllegalArgumentException exception) {
			log.error("MemberCreatedEventConsumer : consume : 잘못된 memberUuid로 초기 등급 부여를 생략 - memberUuid={}",
					event.memberUuid());
		}
	}

	private MemberCreatedEventPayload parse(String payload) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(payload, MemberCreatedEventPayload.class);
		} catch (JsonProcessingException exception) {
			log.error("MemberCreatedEventConsumer : consume : 회원 생성 이벤트 JSON 파싱 실패");
			return null;
		}
	}
}
