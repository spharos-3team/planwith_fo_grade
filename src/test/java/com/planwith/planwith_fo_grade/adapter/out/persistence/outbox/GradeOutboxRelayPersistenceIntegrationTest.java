package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.command.ChangeMemberGradeCommand;
import com.planwith.planwith_fo_grade.application.event.GradeChangedEvent;
import com.planwith.planwith_fo_grade.application.port.in.ChangeMemberGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventPublisher;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.config.GradeKafkaProperties;
import com.planwith.planwith_fo_grade.config.GradeOutboxProperties;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradeOutboxRelayPersistenceIntegrationTest {

	@Autowired
	private ChangeMemberGradeUseCase changeMemberGradeUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private SpringDataGradeOutboxRepository repository;

	@BeforeEach
	void seedCriteria() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
	}

	@Test
	void publishesUnpublishedGradeChangedToKafka() {
		String memberUuid = promoteTravelerToExplorer();
		GradeEventPublisher publisher = mock(GradeEventPublisher.class);
		when(publisher.publish(eq("planwith.grade.changed"), eq(memberUuid), any()))
				.thenReturn(CompletableFuture.completedFuture(null));
		GradeOutboxRelay relay = new GradeOutboxRelay(
				repository,
				publisher,
				new GradeOutboxProperties(),
				new GradeKafkaProperties()
		);

		relay.relayUnpublishedEvents();

		GradeOutboxJpaEntity outbox = unpublished(memberUuid);
		verify(publisher).publish("planwith.grade.changed", memberUuid, outbox.payload());
		assertThat(outbox.publishedAt()).isNotNull();
		assertThat(outbox.retryCount()).isZero();
		assertThat(outbox.eventType()).isEqualTo(GradeChangedEvent.EVENT_TYPE);
	}

	@Test
	void retriesAfterKafkaPublishFailureThenSucceeds() {
		String memberUuid = promoteTravelerToExplorer();
		Instant now = Instant.parse("2026-08-19T06:00:00Z");
		AtomicReference<Instant> clockInstant = new AtomicReference<>(now);
		Clock clock = new Clock() {
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
				return clockInstant.get();
			}
		};
		GradeOutboxProperties properties = new GradeOutboxProperties();
		properties.setBackoffInitial(Duration.ofSeconds(5));
		GradeEventPublisher publisher = mock(GradeEventPublisher.class);
		when(publisher.publish(eq("planwith.grade.changed"), eq(memberUuid), any()))
				.thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka unavailable")))
				.thenReturn(CompletableFuture.completedFuture(null));
		GradeOutboxRelay relay = new GradeOutboxRelay(
				repository,
				publisher,
				properties,
				new GradeKafkaProperties(),
				clock
		);

		relay.relayUnpublishedEvents();
		GradeOutboxJpaEntity outbox = unpublished(memberUuid);
		assertThat(outbox.publishedAt()).isNull();
		assertThat(outbox.retryCount()).isEqualTo(1);

		relay.relayUnpublishedEvents();
		verify(publisher, times(1)).publish(eq("planwith.grade.changed"), eq(memberUuid), any());

		clockInstant.set(now.plusSeconds(5));
		relay.relayUnpublishedEvents();

		verify(publisher, times(2)).publish(eq("planwith.grade.changed"), eq(memberUuid), any());
		assertThat(unpublished(memberUuid).publishedAt()).isEqualTo(now.plusSeconds(5));
	}

	private String promoteTravelerToExplorer() {
		String memberUuid = UUID.randomUUID().toString();
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.TRAVELER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				LocalDateTime.of(2026, 8, 19, 3, 0)
		));
		changeMemberGradeUseCase.change(new ChangeMemberGradeCommand(
				memberUuid,
				GradeCode.TRAVELER.name(),
				GradeCode.EXPLORER.name()
		));
		return memberUuid;
	}

	private GradeOutboxJpaEntity unpublished(String memberUuid) {
		return repository.findUnpublished(PageRequest.of(0, 50)).stream()
				.filter(outbox -> UUID.fromString(memberUuid).equals(outbox.aggregateUuid()))
				.findFirst()
				.or(() -> repository.findAll().stream()
						.filter(outbox -> UUID.fromString(memberUuid).equals(outbox.aggregateUuid()))
						.findFirst())
				.orElseThrow();
	}
}
