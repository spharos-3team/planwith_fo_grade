package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "grade_outbox")
class GradeOutboxJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "outbox_id")
	private Long outboxId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "event_uuid", nullable = false, unique = true, length = 36)
	private UUID eventUuid;

	@Column(name = "aggregate_type", nullable = false, length = 50)
	private String aggregateType;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "aggregate_uuid", nullable = false, length = 36)
	private UUID aggregateUuid;

	@Column(name = "event_type", nullable = false, length = 50)
	private String eventType;

	@Lob
	@Column(name = "payload", nullable = false)
	private String payload;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "next_retry_at")
	private Instant nextRetryAt;

	protected GradeOutboxJpaEntity() {
	}

	GradeOutboxJpaEntity(
			UUID eventUuid,
			String aggregateType,
			UUID aggregateUuid,
			String eventType,
			String payload,
			Instant occurredAt
	) {
		this.eventUuid = eventUuid;
		this.aggregateType = aggregateType;
		this.aggregateUuid = aggregateUuid;
		this.eventType = eventType;
		this.payload = payload;
		this.occurredAt = occurredAt;
		this.retryCount = 0;
	}

	void markPublished(Instant publishedAt) {
		this.publishedAt = publishedAt;
		this.nextRetryAt = null;
	}

	void recordPublishFailure(Instant nextRetryAt) {
		this.retryCount++;
		this.nextRetryAt = nextRetryAt;
	}

	boolean isDue(Instant now) {
		return publishedAt == null && (nextRetryAt == null || !nextRetryAt.isAfter(now));
	}

	Long outboxId() { return outboxId; }
	UUID eventUuid() { return eventUuid; }
	String aggregateType() { return aggregateType; }
	UUID aggregateUuid() { return aggregateUuid; }
	String eventType() { return eventType; }
	String payload() { return payload; }
	Instant occurredAt() { return occurredAt; }
	Instant publishedAt() { return publishedAt; }
	int retryCount() { return retryCount; }
	Instant nextRetryAt() { return nextRetryAt; }
}
