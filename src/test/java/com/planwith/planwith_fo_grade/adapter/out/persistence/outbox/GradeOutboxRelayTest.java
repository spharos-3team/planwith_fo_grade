package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
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
		String payload = gradeChangedPayload(eventUuid, memberUuid);
		GradeOutboxJpaEntity outbox = unpublished(eventUuid, memberUuid, GradeChangedEvent.EVENT_TYPE, payload);
		when(repository.findDueUnpublished(any(Instant.class), any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.changed", memberUuid.toString(), payload))
				.thenReturn(CompletableFuture.completedFuture(null));

		relay.relayUnpublishedEvents();

		verify(publisher).publish("planwith.grade.changed", memberUuid.toString(), payload);
		assertThat(outbox.publishedAt()).isNotNull();
		assertThat(outbox.retryCount()).isZero();
		assertThat(outbox.nextRetryAt()).isNull();
	}

	@Test
	void keepsUnpublishedAndIncrementsRetryCountWhenKafkaPublishFails() {
		SpringDataGradeOutboxRepository repository = mock(SpringDataGradeOutboxRepository.class);
		GradeEventPublisher publisher = mock(GradeEventPublisher.class);
		Instant now = Instant.parse("2026-08-19T06:00:00Z");
		GradeOutboxProperties properties = new GradeOutboxProperties();
		properties.setBackoffInitial(Duration.ofSeconds(5));
		GradeOutboxRelay relay = new GradeOutboxRelay(
				repository,
				publisher,
				properties,
				new GradeKafkaProperties(),
				Clock.fixed(now, ZoneOffset.UTC)
		);
		UUID eventUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		String payload = gradeChangedPayload(eventUuid, memberUuid);
		GradeOutboxJpaEntity outbox = unpublished(eventUuid, memberUuid, GradeChangedEvent.EVENT_TYPE, payload);
		when(repository.findDueUnpublished(eq(now), any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.changed", memberUuid.toString(), payload))
				.thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka unavailable")));

		relay.relayUnpublishedEvents();

		assertThat(outbox.publishedAt()).isNull();
		assertThat(outbox.retryCount()).isEqualTo(1);
		assertThat(outbox.nextRetryAt()).isEqualTo(now.plusSeconds(5));
	}

	@Test
	void skipsRetryUntilBackoffElapsedThenRepublishesSuccessfully() {
		SpringDataGradeOutboxRepository repository = mock(SpringDataGradeOutboxRepository.class);
		GradeEventPublisher publisher = mock(GradeEventPublisher.class);
		AdjustableClock clock = new AdjustableClock(Instant.parse("2026-08-19T06:00:00Z"));
		GradeOutboxProperties properties = new GradeOutboxProperties();
		properties.setBackoffInitial(Duration.ofSeconds(5));
		GradeOutboxRelay relay = new GradeOutboxRelay(
				repository,
				publisher,
				properties,
				new GradeKafkaProperties(),
				clock
		);
		UUID eventUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		String payload = gradeChangedPayload(eventUuid, memberUuid);
		GradeOutboxJpaEntity outbox = unpublished(eventUuid, memberUuid, GradeChangedEvent.EVENT_TYPE, payload);
		when(repository.findDueUnpublished(any(Instant.class), any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.changed", memberUuid.toString(), payload))
				.thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka unavailable")))
				.thenReturn(CompletableFuture.completedFuture(null));

		relay.relayUnpublishedEvents();
		assertThat(outbox.publishedAt()).isNull();
		assertThat(outbox.retryCount()).isEqualTo(1);

		relay.relayUnpublishedEvents();
		verify(publisher, times(1)).publish("planwith.grade.changed", memberUuid.toString(), payload);
		assertThat(outbox.publishedAt()).isNull();

		clock.advance(Duration.ofSeconds(5));
		relay.relayUnpublishedEvents();

		verify(publisher, times(2)).publish("planwith.grade.changed", memberUuid.toString(), payload);
		assertThat(outbox.publishedAt()).isEqualTo(clock.instant());
		assertThat(outbox.nextRetryAt()).isNull();
	}

	@Test
	void keepsUnpublishedEventAfterMaxRetrySoKafkaRecoveryCanRepublish() {
		SpringDataGradeOutboxRepository repository = mock(SpringDataGradeOutboxRepository.class);
		GradeEventPublisher publisher = mock(GradeEventPublisher.class);
		AdjustableClock clock = new AdjustableClock(Instant.parse("2026-08-19T06:00:00Z"));
		GradeOutboxProperties properties = new GradeOutboxProperties();
		properties.setMaxRetry(1);
		properties.setBackoffInitial(Duration.ofSeconds(5));
		properties.setBackoffMax(Duration.ofMinutes(5));
		GradeOutboxRelay relay = new GradeOutboxRelay(
				repository,
				publisher,
				properties,
				new GradeKafkaProperties(),
				clock
		);
		UUID eventUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		String payload = gradeChangedPayload(eventUuid, memberUuid);
		GradeOutboxJpaEntity outbox = unpublished(eventUuid, memberUuid, GradeChangedEvent.EVENT_TYPE, payload);
		when(repository.findDueUnpublished(any(Instant.class), any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.changed", memberUuid.toString(), payload))
				.thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka unavailable")))
				.thenReturn(CompletableFuture.completedFuture(null));

		relay.relayUnpublishedEvents();
		assertThat(outbox.publishedAt()).isNull();
		assertThat(outbox.retryCount()).isEqualTo(1);
		assertThat(outbox.nextRetryAt()).isEqualTo(clock.instant().plus(Duration.ofMinutes(5)));

		clock.advance(Duration.ofMinutes(5));
		relay.relayUnpublishedEvents();

		assertThat(outbox.publishedAt()).isEqualTo(clock.instant());
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
		GradeOutboxJpaEntity outbox = unpublished(eventUuid, memberUuid, GradeRewardGrantedEvent.EVENT_TYPE, payload);
		when(repository.findDueUnpublished(any(Instant.class), any(Pageable.class))).thenReturn(List.of(outbox));
		when(publisher.publish("planwith.grade.reward-granted", memberUuid.toString(), payload))
				.thenReturn(CompletableFuture.completedFuture(null));

		relay.relayUnpublishedEvents();

		verify(publisher).publish("planwith.grade.reward-granted", memberUuid.toString(), payload);
		assertThat(outbox.publishedAt()).isNotNull();
		assertThat(outbox.retryCount()).isZero();
	}

	@Test
	void doesNotPublishAlreadyPublishedOutbox() {
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
		GradeOutboxJpaEntity outbox = unpublished(
				eventUuid,
				memberUuid,
				GradeChangedEvent.EVENT_TYPE,
				gradeChangedPayload(eventUuid, memberUuid)
		);
		outbox.markPublished(Instant.parse("2026-08-19T06:00:00Z"));
		when(repository.findDueUnpublished(any(Instant.class), any(Pageable.class))).thenReturn(List.of(outbox));

		relay.relayUnpublishedEvents();

		verify(publisher, never()).publish(any(), any(), any());
	}

	private static GradeOutboxJpaEntity unpublished(
			UUID eventUuid,
			UUID memberUuid,
			String eventType,
			String payload
	) {
		return new GradeOutboxJpaEntity(
				eventUuid,
				"GradeMember",
				memberUuid,
				eventType,
				payload,
				Instant.parse("2026-08-19T06:00:00Z")
		);
	}

	private static String gradeChangedPayload(UUID eventUuid, UUID memberUuid) {
		return """
				{"eventUuid":"%s","memberUuid":"%s","previousGradeCode":"TRAVELER","currentGradeCode":"EXPLORER","previousGradeLevel":3,"currentGradeLevel":4,"changedAt":"2026-08-19T06:00:00Z"}
				""".formatted(eventUuid, memberUuid).trim();
	}

	private static final class AdjustableClock extends Clock {

		private Instant instant;

		private AdjustableClock(Instant instant) {
			this.instant = instant;
		}

		private void advance(Duration duration) {
			this.instant = this.instant.plus(duration);
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(java.time.ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return instant;
		}
	}
}
