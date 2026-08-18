package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "grade_condition")
class GradeConditionJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "condition_id")
	private Long conditionId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "grade_id", nullable = false)
	private GradeJpaEntity grade;

	@Enumerated(EnumType.STRING)
	@Column(name = "metric_type", nullable = false, length = 30)
	private GradeMetricType metricType;

	@Column(name = "condition_name", nullable = false, length = 100)
	private String conditionName;

	@Column(name = "threshold_value", nullable = false)
	private long thresholdValue;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(columnDefinition = "TEXT")
	private String description;

	protected GradeConditionJpaEntity() {
	}

	Long getConditionId() {
		return conditionId;
	}

	Long getGradeId() {
		return grade == null ? null : grade.getGradeId();
	}

	GradeMetricType getMetricType() {
		return metricType;
	}

	String getConditionName() {
		return conditionName;
	}

	long getThresholdValue() {
		return thresholdValue;
	}

	int getSortOrder() {
		return sortOrder;
	}

	String getDescription() {
		return description;
	}

	void assignGrade(GradeJpaEntity grade) {
		this.grade = grade;
	}

	void updateDetails(
			GradeMetricType metricType,
			String conditionName,
			long thresholdValue,
			int sortOrder,
			String description
	) {
		this.metricType = metricType;
		this.conditionName = conditionName;
		this.thresholdValue = thresholdValue;
		this.sortOrder = sortOrder;
		this.description = description;
	}
}
