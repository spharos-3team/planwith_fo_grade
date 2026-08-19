package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.ArrayList;
import java.util.List;

import com.planwith.planwith_fo_grade.domain.model.GradeCode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "grade")
class GradeJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "grade_id")
	private Long gradeId;

	@Enumerated(EnumType.STRING)
	@Column(name = "grade_code", nullable = false, unique = true, length = 20)
	private GradeCode gradeCode;

	@Column(name = "grade_name", nullable = false, length = 50)
	private String gradeName;

	@Column(name = "grade_level", nullable = false, unique = true)
	private int gradeLevel;

	@Column(columnDefinition = "TEXT")
	private String description;

	@OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("sortOrder asc")
	private final List<GradeConditionJpaEntity> conditions = new ArrayList<>();

	@OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("sortOrder asc")
	private final List<GradeBenefitJpaEntity> benefits = new ArrayList<>();

	protected GradeJpaEntity() {
	}

	Long getGradeId() {
		return gradeId;
	}

	GradeCode getGradeCode() {
		return gradeCode;
	}

	String getGradeName() {
		return gradeName;
	}

	int getGradeLevel() {
		return gradeLevel;
	}

	String getDescription() {
		return description;
	}

	List<GradeConditionJpaEntity> getConditions() {
		return List.copyOf(conditions);
	}

	List<GradeBenefitJpaEntity> getBenefits() {
		return List.copyOf(benefits);
	}

	void assignGradeId(Long gradeId) {
		this.gradeId = gradeId;
	}

	void updateDetails(GradeCode gradeCode, String gradeName, int gradeLevel, String description) {
		this.gradeCode = gradeCode;
		this.gradeName = gradeName;
		this.gradeLevel = gradeLevel;
		this.description = description;
	}

	void replaceConditions(List<GradeConditionJpaEntity> newConditions) {
		conditions.clear();
		for (GradeConditionJpaEntity condition : newConditions) {
			addCondition(condition);
		}
	}

	void replaceBenefits(List<GradeBenefitJpaEntity> newBenefits) {
		benefits.clear();
		for (GradeBenefitJpaEntity benefit : newBenefits) {
			addBenefit(benefit);
		}
	}

	void addCondition(GradeConditionJpaEntity condition) {
		conditions.add(condition);
		condition.assignGrade(this);
	}

	void addBenefit(GradeBenefitJpaEntity benefit) {
		benefits.add(benefit);
		benefit.assignGrade(this);
	}
}
