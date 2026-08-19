package com.planwith.planwith_fo_grade.adapter.out.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaGradeEventPublisherTest {

	@Test
	void sendsPayloadToKafkaWithEventUuidAsKey() {
		@SuppressWarnings("unchecked")
		KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
		when(kafkaTemplate.send("planwith.grade.changed", "event-uuid", "{\"currentGrade\":\"EXPLORER\"}"))
				.thenReturn(CompletableFuture.completedFuture(null));
		KafkaGradeEventPublisher publisher = new KafkaGradeEventPublisher(kafkaTemplate);

		publisher.publish("planwith.grade.changed", "event-uuid", "{\"currentGrade\":\"EXPLORER\"}").join();

		verify(kafkaTemplate).send("planwith.grade.changed", "event-uuid", "{\"currentGrade\":\"EXPLORER\"}");
	}
}
