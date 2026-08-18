package com.planwith.planwith_fo_grade.adapter.in.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "grade.kafka.consumer-enabled", havingValue = "true")
public class GradeInboundEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(GradeInboundEventConsumer.class);

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
		log.debug("GradeInboundEventConsumer : consume : 이벤트 payload 확인 - payloadLength={}",
				payload == null ? 0 : payload.length());
	}
}
