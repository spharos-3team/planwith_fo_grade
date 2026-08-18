package com.planwith.planwith_fo_grade.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

public final class GradeMember {

	private final Long gradeId;
	private final MemberUuid memberUuid;
	private final GradeStatus gradeStatus;
	private final LocalDateTime gradeAssignedAt;
	private final LocalDateTime lastEvaluatedAt;

	private GradeMember(
			Long gradeId,
			MemberUuid memberUuid,
			GradeStatus gradeStatus,
			LocalDateTime gradeAssignedAt,
			LocalDateTime lastEvaluatedAt
	) {
		this.gradeId = Objects.requireNonNull(gradeId, "Grade ID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.gradeStatus = Objects.requireNonNull(gradeStatus, "Grade status is required.");
		this.gradeAssignedAt = Objects.requireNonNull(gradeAssignedAt, "Grade assigned at is required.");
		this.lastEvaluatedAt = lastEvaluatedAt;
	}

	public static GradeMember assign(
			Long gradeId,
			MemberUuid memberUuid,
			LocalDateTime assignedAt
	) {
		return new GradeMember(gradeId, memberUuid, GradeStatus.ACTIVE, assignedAt, null);
	}

	public static GradeMember reconstitute(
			Long gradeId,
			MemberUuid memberUuid,
			GradeStatus gradeStatus,
			LocalDateTime gradeAssignedAt,
			LocalDateTime lastEvaluatedAt
	) {
		return new GradeMember(gradeId, memberUuid, gradeStatus, gradeAssignedAt, lastEvaluatedAt);
	}

	public GradeMember changeGrade(Long newGradeId, LocalDateTime changedAt) {
		Objects.requireNonNull(newGradeId, "New grade ID is required.");
		Objects.requireNonNull(changedAt, "Changed at is required.");
		if (gradeStatus != GradeStatus.ACTIVE) {
			throw new InvalidGradeException("Only active members can change grade.");
		}
		return new GradeMember(newGradeId, memberUuid, gradeStatus, changedAt, changedAt);
	}

	public GradeMember markEvaluated(LocalDateTime evaluatedAt) {
		Objects.requireNonNull(evaluatedAt, "Evaluated at is required.");
		if (gradeStatus != GradeStatus.ACTIVE) {
			throw new InvalidGradeException("Only active members can be evaluated.");
		}
		return new GradeMember(gradeId, memberUuid, gradeStatus, gradeAssignedAt, evaluatedAt);
	}

	public GradeMember suspend() {
		if (gradeStatus == GradeStatus.WITHDRAWN) {
			throw new InvalidGradeException("Withdrawn members cannot be suspended.");
		}
		return new GradeMember(gradeId, memberUuid, GradeStatus.SUSPENDED, gradeAssignedAt, lastEvaluatedAt);
	}

	public GradeMember withdraw() {
		return new GradeMember(gradeId, memberUuid, GradeStatus.WITHDRAWN, gradeAssignedAt, lastEvaluatedAt);
	}

	public boolean isActive() {
		return gradeStatus == GradeStatus.ACTIVE;
	}

	public Long gradeId() {
		return gradeId;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public GradeStatus gradeStatus() {
		return gradeStatus;
	}

	public LocalDateTime gradeAssignedAt() {
		return gradeAssignedAt;
	}

	public LocalDateTime lastEvaluatedAt() {
		return lastEvaluatedAt;
	}
}
