package com.planwith.planwith_fo_grade.adapter.out.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaGradeEventPublisherTest {

	@Test
	void sendsPayloadToKafkaWithMemberUuidAsKey() {
		@SuppressWarnings("unchecked")
		KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
		String payload = "{\"eventUuid\":\"event-uuid\",\"memberUuid\":\"member-uuid\",\"previousGradeCode\":\"TRAVELER\",\"currentGradeCode\":\"EXPLORER\",\"previousGradeLevel\":3,\"currentGradeLevel\":4,\"changedAt\":\"2026-08-19T06:00:00Z\"}";
		when(kafkaTemplate.send("planwith.grade.changed", "member-uuid", payload))
				.thenReturn(CompletableFuture.completedFuture(null));
		KafkaGradeEventPublisher publisher = new KafkaGradeEventPublisher(kafkaTemplate);

		publisher.publish("planwith.grade.changed", "member-uuid", payload).join();

		verify(kafkaTemplate).send("planwith.grade.changed", "member-uuid", payload);
	}
}
