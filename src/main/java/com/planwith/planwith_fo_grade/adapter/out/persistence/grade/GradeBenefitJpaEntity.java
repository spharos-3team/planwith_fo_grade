package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import com.planwith.planwith_fo_grade.domain.model.BenefitCode;

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
@Table(name = "grade_benefit")
class GradeBenefitJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "benefit_id")
	private Long benefitId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "grade_id", nullable = false)
	private GradeJpaEntity grade;

	@Enumerated(EnumType.STRING)
	@Column(name = "benefit_code", nullable = false, length = 30)
	private BenefitCode benefitCode;

	@Column(name = "benefit_name", nullable = false, length = 100)
	private String benefitName;

	@Column(name = "benefit_value", nullable = false, length = 200)
	private String benefitValue;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	protected GradeBenefitJpaEntity() {
	}

	Long getBenefitId() {
		return benefitId;
	}

	Long getGradeId() {
		return grade == null ? null : grade.getGradeId();
	}

	BenefitCode getBenefitCode() {
		return benefitCode;
	}

	String getBenefitName() {
		return benefitName;
	}

	String getBenefitValue() {
		return benefitValue;
	}

	String getDescription() {
		return description;
	}

	int getSortOrder() {
		return sortOrder;
	}

	void assignGrade(GradeJpaEntity grade) {
		this.grade = grade;
	}

	void updateDetails(
			BenefitCode benefitCode,
			String benefitName,
			String benefitValue,
			String description,
			int sortOrder
	) {
		this.benefitCode = benefitCode;
		this.benefitName = benefitName;
		this.benefitValue = benefitValue;
		this.description = description;
		this.sortOrder = sortOrder;
	}
}
