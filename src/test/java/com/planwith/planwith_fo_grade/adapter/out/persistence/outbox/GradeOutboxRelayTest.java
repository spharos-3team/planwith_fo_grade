package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.planwith.planwith_fo_grade.application.port.out.GradeEventPublisher;
import com.planwith.planwith_fo_grade.config.GradeKafkaProperties;
import com.planwith.planwith_fo_grade.config.GradeOutboxProperties;
import com.planwith.planwith_fo_grade.domain.event.GradeEventType;

class GradeOutboxRelayTest {

	@Test
	void publishesGradeChangedPayloadToGradeChangedTopic() {
		SpringDataGradeOutboxRepository repository = mock(SpringDataGradeOutboxRepository.class);
		GradeEventPublisher publisher = mock(GradeEventPublisher.class);
		GradeOutboxRelay relay = new GradeOutboxRelay(
				repository,
				publisher,
				new GradeOutboxProperties(),
				new GradeKafkaProperties()
		);
		UUID eventUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		String payload = """
				{"eventUuid":"%s","memberUuid":"%s","previousGrade":"TRAVELER","currentGrade":"EXPLORER","gradeLevel":4,"changedAt":"2026-08-19T06:00:00Z"}
				""".formatted(eventUuid, memberUuid).trim();
		GradeOutboxJpaEntity outbox = new GradeOutboxJpaEntity(
				eventUuid,
				"GradeMember",
				memberUuid,
				GradeEventType.GRADE_CHANGED.name(),
				payload,
				Instant.parse("2026-08-19T06:00:00Z")
		);
		when(repository.findUnpublished(any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.changed", eventUuid.toString(), payload))
				.thenReturn(CompletableFuture.completedFuture(null));

		relay.relayUnpublishedEvents();

		verify(publisher).publish("planwith.grade.changed", eventUuid.toString(), payload);
		assertThat(outbox.publishedAt()).isNotNull();
		assertThat(outbox.retryCount()).isZero();
	}
}
