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

import com.planwith.planwith_fo_grade.application.event.GradeChangedEvent;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventPublisher;
import com.planwith.planwith_fo_grade.config.GradeKafkaProperties;
import com.planwith.planwith_fo_grade.config.GradeOutboxProperties;

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
				{"eventUuid":"%s","memberUuid":"%s","previousGradeCode":"TRAVELER","currentGradeCode":"EXPLORER","previousGradeLevel":3,"currentGradeLevel":4,"changedAt":"2026-08-19T06:00:00Z"}
				""".formatted(eventUuid, memberUuid).trim();
		GradeOutboxJpaEntity outbox = new GradeOutboxJpaEntity(
				eventUuid,
				"GradeMember",
				memberUuid,
				GradeChangedEvent.EVENT_TYPE,
				payload,
				Instant.parse("2026-08-19T06:00:00Z")
		);
		when(repository.findUnpublished(any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.changed", memberUuid.toString(), payload))
				.thenReturn(CompletableFuture.completedFuture(null));

		relay.relayUnpublishedEvents();

		verify(publisher).publish("planwith.grade.changed", memberUuid.toString(), payload);
		assertThat(outbox.publishedAt()).isNotNull();
		assertThat(outbox.retryCount()).isZero();
	}

	@Test
	void keepsUnpublishedAndIncrementsRetryCountWhenKafkaPublishFails() {
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
				{"eventUuid":"%s","memberUuid":"%s","previousGradeCode":"TRAVELER","currentGradeCode":"EXPLORER","previousGradeLevel":3,"currentGradeLevel":4,"changedAt":"2026-08-19T06:00:00Z"}
				""".formatted(eventUuid, memberUuid).trim();
		GradeOutboxJpaEntity outbox = new GradeOutboxJpaEntity(
				eventUuid,
				"GradeMember",
				memberUuid,
				GradeChangedEvent.EVENT_TYPE,
				payload,
				Instant.parse("2026-08-19T06:00:00Z")
		);
		when(repository.findUnpublished(any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.changed", memberUuid.toString(), payload))
				.thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka unavailable")));

		relay.relayUnpublishedEvents();

		assertThat(outbox.publishedAt()).isNull();
		assertThat(outbox.retryCount()).isEqualTo(1);
	}

	@Test
	void publishesGradeRewardGrantedPayloadToRewardGrantedTopic() {
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
				{"eventUuid":"%s","memberUuid":"%s","gradeCode":"EXPLORER","gradeLevel":4,"rewardMonth":"2026-08","tokenAmount":50,"rewardType":"MONTHLY_FREE_TOKEN","grantedAt":"2026-08-19T06:00:00Z"}
				""".formatted(eventUuid, memberUuid).trim();
		GradeOutboxJpaEntity outbox = new GradeOutboxJpaEntity(
				eventUuid,
				"GradeRewardHistory",
				memberUuid,
				GradeRewardGrantedEvent.EVENT_TYPE,
				payload,
				Instant.parse("2026-08-19T06:00:00Z")
		);
		when(repository.findUnpublished(any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.reward-granted", memberUuid.toString(), payload))
				.thenReturn(CompletableFuture.completedFuture(null));

		relay.relayUnpublishedEvents();

		verify(publisher).publish("planwith.grade.reward-granted", memberUuid.toString(), payload);
		assertThat(outbox.publishedAt()).isNotNull();
		assertThat(outbox.retryCount()).isZero();
	}
}
