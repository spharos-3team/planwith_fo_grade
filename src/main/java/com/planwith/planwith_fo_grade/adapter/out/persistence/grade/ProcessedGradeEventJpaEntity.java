package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "processed_grade_event",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_processed_grade_event_uuid",
				columnNames = {"event_uuid"}
		)
)
class ProcessedGradeEventJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "processed_id")
	private Long processedId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "event_uuid", nullable = false, length = 36)
	private UUID eventUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "metric_type", nullable = false, length = 30)
	private MemberMetricType metricType;

	@Column(name = "processed_at", nullable = false)
	private LocalDateTime processedAt;

	protected ProcessedGradeEventJpaEntity() {
	}

	static ProcessedGradeEventJpaEntity create(
			UUID eventUuid,
			UUID memberUuid,
			MemberMetricType metricType,
			LocalDateTime processedAt
	) {
		ProcessedGradeEventJpaEntity entity = new ProcessedGradeEventJpaEntity();
		entity.eventUuid = eventUuid;
		entity.memberUuid = memberUuid;
		entity.metricType = metricType;
		entity.processedAt = processedAt;
		return entity;
	}
}
