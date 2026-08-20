package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.event.GradeChangedEvent;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
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
	private final Clock clock;

	@Autowired
	public GradeOutboxRelay(
			SpringDataGradeOutboxRepository repository,
			GradeEventPublisher publisher,
			GradeOutboxProperties outboxProperties,
			GradeKafkaProperties kafkaProperties
	) {
		this(repository, publisher, outboxProperties, kafkaProperties, Clock.systemUTC());
	}

	GradeOutboxRelay(
			SpringDataGradeOutboxRepository repository,
			GradeEventPublisher publisher,
			GradeOutboxProperties outboxProperties,
			GradeKafkaProperties kafkaProperties,
			Clock clock
	) {
		this.repository = repository;
		this.publisher = publisher;
		this.outboxProperties = outboxProperties;
		this.kafkaProperties = kafkaProperties;
		this.clock = clock;
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
		Instant now = clock.instant();
		List<GradeOutboxJpaEntity> unpublished = repository.findDueUnpublished(now, PageRequest.of(0, batchSize));
		for (GradeOutboxJpaEntity outbox : unpublished) {
			if (outbox.isDue(now)) {
				publish(outbox, now);
			}
		}
	}

	private void publish(GradeOutboxJpaEntity outbox, Instant now) {
		try {
			publisher.publish(
							topicFor(outbox.eventType()),
							outbox.aggregateUuid().toString(),
							outbox.payload()
					)
					.get(sendTimeoutMillis(), TimeUnit.MILLISECONDS);
			outbox.markPublished(now);
			log.info("GradeOutboxRelay : publish : 등급 Outbox 발행 완료 - eventUuid={}, eventType={}",
					outbox.eventUuid(), outbox.eventType());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			recordFailure(outbox, now);
			log.warn("GradeOutboxRelay : publish : 등급 Outbox 발행 중단 - eventUuid={}, retryCount={}",
					outbox.eventUuid(), outbox.retryCount());
		} catch (Exception exception) {
			recordFailure(outbox, now);
			if (outboxProperties.retryLimitReached(outbox.retryCount())) {
				log.error(
						"GradeOutboxRelay : publish : 등급 Outbox 최대 재시도 이후에도 미발행 유지 - eventUuid={}, retryCount={}",
						outbox.eventUuid(),
						outbox.retryCount()
				);
			} else {
				log.warn("GradeOutboxRelay : publish : 등급 Outbox 발행 실패 - eventUuid={}, retryCount={}",
						outbox.eventUuid(), outbox.retryCount());
			}
		}
	}

	private void recordFailure(GradeOutboxJpaEntity outbox, Instant now) {
		int nextRetryCount = outbox.retryCount() + 1;
		outbox.recordPublishFailure(outboxProperties.nextRetryAt(now, nextRetryCount));
	}

	private String topicFor(String eventType) {
		if (GradeRewardGrantedEvent.EVENT_TYPE.equals(eventType)
				|| GradeEventType.GRADE_REWARD_GRANTED.name().equals(eventType)) {
			return kafkaProperties.getTopics().getGradeRewardGranted();
		}
		if (!GradeChangedEvent.EVENT_TYPE.equals(eventType)
				&& !GradeEventType.GRADE_CHANGED.name().equals(eventType)) {
			log.warn("GradeOutboxRelay : topicFor : 알 수 없는 Outbox eventType이라 grade.changed로 발행 - eventType={}",
					eventType);
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
