package com.planwith.planwith_fo_grade.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_grade.application.port.out.GradeEventPublisher;

@Component
public class KafkaGradeEventPublisher implements GradeEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaGradeEventPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaGradeEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public CompletableFuture<Void> publish(String topic, String eventUuid, String payload) {
		log.info("KafkaGradeEventPublisher : publish : 등급 이벤트 Kafka 발행 시작 - topic={}, eventUuid={}",
				topic, eventUuid);
		return kafkaTemplate.send(topic, eventUuid, payload)
				.thenAccept(result -> log.info(
						"KafkaGradeEventPublisher : publish : 등급 이벤트 Kafka 발행 완료 - topic={}, eventUuid={}",
						topic, eventUuid
				));
	}
}
