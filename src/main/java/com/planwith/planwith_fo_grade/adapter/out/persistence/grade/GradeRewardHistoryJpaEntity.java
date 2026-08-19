package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_grade.domain.model.RewardStatus;

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
		name = "grade_reward_history",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_grade_reward_member_month",
				columnNames = {"member_uuid", "reward_month"}
		)
)
class GradeRewardHistoryJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reward_id")
	private Long rewardId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "grade_id", nullable = false)
	private Long gradeId;

	@Column(name = "reward_month", nullable = false, length = 7)
	private String rewardMonth;

	@Column(name = "token_amount", nullable = false)
	private long tokenAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "reward_status", nullable = false, length = 20)
	private RewardStatus rewardStatus;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected GradeRewardHistoryJpaEntity() {
	}

	Long getRewardId() {
		return rewardId;
	}

	UUID getMemberUuid() {
		return memberUuid;
	}

	Long getGradeId() {
		return gradeId;
	}

	String getRewardMonth() {
		return rewardMonth;
	}

	long getTokenAmount() {
		return tokenAmount;
	}

	RewardStatus getRewardStatus() {
		return rewardStatus;
	}

	LocalDateTime getCreatedAt() {
		return createdAt;
	}

	void updateDetails(long tokenAmount, RewardStatus rewardStatus) {
		this.tokenAmount = tokenAmount;
		this.rewardStatus = rewardStatus;
	}

	static GradeRewardHistoryJpaEntity createNew(
			UUID memberUuid,
			Long gradeId,
			String rewardMonth,
			LocalDateTime createdAt
	) {
		GradeRewardHistoryJpaEntity entity = new GradeRewardHistoryJpaEntity();
		entity.memberUuid = memberUuid;
		entity.gradeId = gradeId;
		entity.rewardMonth = rewardMonth;
		entity.createdAt = createdAt;
		return entity;
	}
}
