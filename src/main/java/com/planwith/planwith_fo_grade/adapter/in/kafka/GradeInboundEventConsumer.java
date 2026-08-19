package com.planwith.planwith_fo_grade.adapter.in.kafka;

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
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.FollowCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.FollowRemovedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.LikeCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.LikeRemovedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.StoryCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.StoryDeletedEventPayload;
import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;
import com.planwith.planwith_fo_grade.application.port.in.RecordGradeMetricUseCase;
import com.planwith.planwith_fo_grade.config.GradeKafkaProperties;

@Component
@ConditionalOnProperty(name = "grade.kafka.consumer-enabled", havingValue = "true")
public class GradeInboundEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(GradeInboundEventConsumer.class);

	private final RecordGradeMetricUseCase recordGradeMetricUseCase;
	private final ObjectMapper objectMapper;
	private final GradeKafkaProperties.Topics topics;

	public GradeInboundEventConsumer(
			RecordGradeMetricUseCase recordGradeMetricUseCase,
			ObjectMapper objectMapper,
			GradeKafkaProperties kafkaProperties
	) {
		this.recordGradeMetricUseCase = recordGradeMetricUseCase;
		this.objectMapper = objectMapper;
		this.topics = kafkaProperties.getTopics();
	}

	@KafkaListener(
			topics = {
					"${grade.kafka.topics.story-created}",
					"${grade.kafka.topics.story-deleted}",
					"${grade.kafka.topics.follow-created}",
					"${grade.kafka.topics.follow-removed}",
					"${grade.kafka.topics.like-created}",
					"${grade.kafka.topics.like-removed}"
			}
	)
	public void consume(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		log.info("GradeInboundEventConsumer : consume : 등급 입력 이벤트 수신 - topic={}", topic);
		try {
			RecordGradeMetricCommand command = toCommand(topic, payload);
			if (command == null) {
				log.error("GradeInboundEventConsumer : consume : Metric 이벤트 파싱 실패로 갱신을 생략 - topic={}", topic);
				return;
			}
			log.debug(
					"GradeInboundEventConsumer : consume : Metric 갱신 요청 확인 - memberUuid={}, metricType={}, delta={}",
					command.memberUuid(),
					command.metricType(),
					command.delta()
			);
			recordGradeMetricUseCase.record(command);
		} catch (IllegalArgumentException exception) {
			log.error("GradeInboundEventConsumer : consume : 잘못된 Metric 이벤트로 갱신을 생략 - topic={}", topic);
		} catch (RuntimeException exception) {
			log.error("GradeInboundEventConsumer : consume : Metric 이벤트 처리 실패로 재처리 대기 - topic={}", topic);
			throw exception;
		}
	}

	private RecordGradeMetricCommand toCommand(String topic, String payload) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		if (topics.getStoryCreated().equals(topic)) {
			StoryCreatedEventPayload event = parse(payload, StoryCreatedEventPayload.class);
			return event == null ? null : MetricInboundEventMapper.fromStoryCreated(event);
		}
		if (topics.getStoryDeleted().equals(topic)) {
			StoryDeletedEventPayload event = parse(payload, StoryDeletedEventPayload.class);
			return event == null ? null : MetricInboundEventMapper.fromStoryDeleted(event);
		}
		if (topics.getFollowCreated().equals(topic)) {
			FollowCreatedEventPayload event = parse(payload, FollowCreatedEventPayload.class);
			return event == null ? null : MetricInboundEventMapper.fromFollowCreated(event);
		}
		if (topics.getFollowRemoved().equals(topic)) {
			FollowRemovedEventPayload event = parse(payload, FollowRemovedEventPayload.class);
			return event == null ? null : MetricInboundEventMapper.fromFollowRemoved(event);
		}
		if (topics.getLikeCreated().equals(topic)) {
			LikeCreatedEventPayload event = parse(payload, LikeCreatedEventPayload.class);
			return event == null ? null : MetricInboundEventMapper.fromLikeCreated(event);
		}
		if (topics.getLikeRemoved().equals(topic)) {
			LikeRemovedEventPayload event = parse(payload, LikeRemovedEventPayload.class);
			return event == null ? null : MetricInboundEventMapper.fromLikeRemoved(event);
		}
		log.warn("GradeInboundEventConsumer : consume : 지원하지 않는 Metric 토픽 - topic={}", topic);
		return null;
	}

	private <T> T parse(String payload, Class<T> type) {
		try {
			return objectMapper.readValue(payload, type);
		} catch (JsonProcessingException exception) {
			log.error("GradeInboundEventConsumer : consume : Metric 이벤트 JSON 파싱 실패 - payloadType={}",
					type.getSimpleName());
			return null;
		}
	}
}
