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
		name = "member_grade_metric",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_member_grade_metric_member_type",
				columnNames = {"member_uuid", "metric_type"}
		)
)
class MemberGradeMetricJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "metric_id")
	private Long metricId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "metric_type", nullable = false, length = 30)
	private MemberMetricType metricType;

	@Column(name = "current_value", nullable = false)
	private long currentValue;

	@Column(name = "source_service", nullable = false, length = 50)
	private String sourceService;

	@Column(name = "source_version", nullable = false)
	private long sourceVersion;

	@Column(name = "synchronized_at", nullable = false)
	private LocalDateTime synchronizedAt;

	protected MemberGradeMetricJpaEntity() {
	}

	Long getMetricId() {
		return metricId;
	}

	UUID getMemberUuid() {
		return memberUuid;
	}

	MemberMetricType getMetricType() {
		return metricType;
	}

	long getCurrentValue() {
		return currentValue;
	}

	String getSourceService() {
		return sourceService;
	}

	long getSourceVersion() {
		return sourceVersion;
	}

	LocalDateTime getSynchronizedAt() {
		return synchronizedAt;
	}

	void updateDetails(
			long currentValue,
			String sourceService,
			long sourceVersion,
			LocalDateTime synchronizedAt
	) {
		this.currentValue = currentValue;
		this.sourceService = sourceService;
		this.sourceVersion = sourceVersion;
		this.synchronizedAt = synchronizedAt;
	}

	static MemberGradeMetricJpaEntity createNew(UUID memberUuid, MemberMetricType metricType) {
		MemberGradeMetricJpaEntity entity = new MemberGradeMetricJpaEntity();
		entity.memberUuid = memberUuid;
		entity.metricType = metricType;
		return entity;
	}
}
