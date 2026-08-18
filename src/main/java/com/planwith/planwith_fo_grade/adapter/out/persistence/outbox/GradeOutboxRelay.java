package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.out.GradeEventPublisher;
import com.planwith.planwith_fo_grade.config.GradeKafkaProperties;
import com.planwith.planwith_fo_grade.config.GradeOutboxProperties;
import com.planwith.planwith_fo_grade.domain.event.GradeEventType;

@Component
@ConditionalOnProperty(name = "grade.outbox.enabled", havingValue = "true")
public class GradeOutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(GradeOutboxRelay.class);

	private final SpringDataGradeOutboxRepository repository;
	private final GradeEventPublisher publisher;
	private final GradeOutboxProperties outboxProperties;
	private final GradeKafkaProperties kafkaProperties;

	public GradeOutboxRelay(
			SpringDataGradeOutboxRepository repository,
			GradeEventPublisher publisher,
			GradeOutboxProperties outboxProperties,
			GradeKafkaProperties kafkaProperties
	) {
		this.repository = repository;
		this.publisher = publisher;
		this.outboxProperties = outboxProperties;
		this.kafkaProperties = kafkaProperties;
	}

	@Scheduled(
			fixedDelayString = "${grade.outbox.relay-interval:5s}",
			initialDelayString = "${grade.outbox.relay-initial-delay:5s}"
	)
	@Transactional
	public void relayUnpublishedEvents() {
		int batchSize = outboxProperties.getRelayBatchSize() > 0
				? outboxProperties.getRelayBatchSize()
				: 50;
		List<GradeOutboxJpaEntity> unpublished = repository.findUnpublished(PageRequest.of(0, batchSize));
		for (GradeOutboxJpaEntity outbox : unpublished) {
			publish(outbox);
		}
	}

	private void publish(GradeOutboxJpaEntity outbox) {
		try {
			publisher.publish(topicFor(outbox.eventType()), outbox.eventUuid().toString(), outbox.payload())
					.get(sendTimeoutMillis(), TimeUnit.MILLISECONDS);
			outbox.markPublished(Instant.now());
			log.info("GradeOutboxRelay : publish : 등급 Outbox 발행 완료 - eventUuid={}, eventType={}",
					outbox.eventUuid(), outbox.eventType());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			outbox.increaseRetryCount();
			log.warn("GradeOutboxRelay : publish : 등급 Outbox 발행 중단 - eventUuid={}", outbox.eventUuid());
		} catch (Exception exception) {
			outbox.increaseRetryCount();
			log.warn("GradeOutboxRelay : publish : 등급 Outbox 발행 실패 - eventUuid={}, retryCount={}",
					outbox.eventUuid(), outbox.retryCount());
		}
	}

	private String topicFor(String eventType) {
		if (GradeEventType.GRADE_REWARD_GRANTED.name().equals(eventType)) {
			return kafkaProperties.getTopics().getGradeRewardGranted();
		}
		return kafkaProperties.getTopics().getGradeChanged();
	}

	private long sendTimeoutMillis() {
		Duration timeout = outboxProperties.getSendTimeout();
		if (timeout == null || timeout.isZero() || timeout.isNegative()) {
			return Duration.ofSeconds(10).toMillis();
		}
		return timeout.toMillis();
	}
}
