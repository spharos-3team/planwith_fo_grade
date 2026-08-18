package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_grade.domain.model.GradeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "grade_member")
class GradeMemberJpaEntity {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "grade_id", nullable = false)
	private Long gradeId;

	@Enumerated(EnumType.STRING)
	@Column(name = "grade_status", nullable = false, length = 20)
	private GradeStatus gradeStatus;

	@Column(name = "grade_assigned_at", nullable = false)
	private LocalDateTime gradeAssignedAt;

	@Column(name = "last_evaluated_at")
	private LocalDateTime lastEvaluatedAt;

	protected GradeMemberJpaEntity() {
	}

	UUID getMemberUuid() {
		return memberUuid;
	}

	Long getGradeId() {
		return gradeId;
	}

	GradeStatus getGradeStatus() {
		return gradeStatus;
	}

	LocalDateTime getGradeAssignedAt() {
		return gradeAssignedAt;
	}

	LocalDateTime getLastEvaluatedAt() {
		return lastEvaluatedAt;
	}

	void updateDetails(
			Long gradeId,
			GradeStatus gradeStatus,
			LocalDateTime gradeAssignedAt,
			LocalDateTime lastEvaluatedAt
	) {
		this.gradeId = gradeId;
		this.gradeStatus = gradeStatus;
		this.gradeAssignedAt = gradeAssignedAt;
		this.lastEvaluatedAt = lastEvaluatedAt;
	}

	static GradeMemberJpaEntity createNew(UUID memberUuid) {
		GradeMemberJpaEntity entity = new GradeMemberJpaEntity();
		entity.memberUuid = memberUuid;
		return entity;
	}
}
