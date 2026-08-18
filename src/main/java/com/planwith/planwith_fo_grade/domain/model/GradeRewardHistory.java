package com.planwith.planwith_fo_grade.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

public final class GradeRewardHistory {

	private static final Pattern REWARD_MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");

	private final Long rewardId;
	private final MemberUuid memberUuid;
	private final Long gradeId;
	private final String rewardMonth;
	private final long tokenAmount;
	private final RewardStatus rewardStatus;
	private final LocalDateTime createdAt;

	private GradeRewardHistory(
			Long rewardId,
			MemberUuid memberUuid,
			Long gradeId,
			String rewardMonth,
			long tokenAmount,
			RewardStatus rewardStatus,
			LocalDateTime createdAt
	) {
		this.rewardId = rewardId;
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.gradeId = Objects.requireNonNull(gradeId, "Grade ID is required.");
		this.rewardMonth = requireRewardMonth(rewardMonth);
		if (tokenAmount < 0) {
			throw new InvalidGradeException("Token amount must not be negative.");
		}
		this.tokenAmount = tokenAmount;
		this.rewardStatus = Objects.requireNonNull(rewardStatus, "Reward status is required.");
		this.createdAt = Objects.requireNonNull(createdAt, "Created at is required.");
	}

	public static GradeRewardHistory create(
			MemberUuid memberUuid,
			Long gradeId,
			String rewardMonth,
			long tokenAmount,
			LocalDateTime createdAt
	) {
		return new GradeRewardHistory(
				null, memberUuid, gradeId, rewardMonth, tokenAmount, RewardStatus.COMPLETED, createdAt
		);
	}

	public static GradeRewardHistory createReady(
			MemberUuid memberUuid,
			Long gradeId,
			String rewardMonth,
			long tokenAmount,
			LocalDateTime createdAt
	) {
		return new GradeRewardHistory(
				null, memberUuid, gradeId, rewardMonth, tokenAmount, RewardStatus.READY, createdAt
		);
	}

	public static GradeRewardHistory reconstitute(
			Long rewardId,
			MemberUuid memberUuid,
			Long gradeId,
			String rewardMonth,
			long tokenAmount,
			RewardStatus rewardStatus,
			LocalDateTime createdAt
	) {
		Objects.requireNonNull(rewardId, "Reward ID is required.");
		return new GradeRewardHistory(
				rewardId, memberUuid, gradeId, rewardMonth, tokenAmount, rewardStatus, createdAt
		);
	}

	public GradeRewardHistory complete() {
		if (rewardStatus != RewardStatus.READY) {
			throw new InvalidGradeException("Only ready rewards can be completed.");
		}
		return new GradeRewardHistory(
				rewardId, memberUuid, gradeId, rewardMonth, tokenAmount, RewardStatus.COMPLETED, createdAt
		);
	}

	public GradeRewardHistory fail() {
		if (rewardStatus != RewardStatus.READY) {
			throw new InvalidGradeException("Only ready rewards can be marked failed.");
		}
		return new GradeRewardHistory(
				rewardId, memberUuid, gradeId, rewardMonth, tokenAmount, RewardStatus.FAILED, createdAt
		);
	}

	public GradeRewardHistory cancel() {
		if (rewardStatus == RewardStatus.COMPLETED) {
			throw new InvalidGradeException("Completed rewards cannot be canceled.");
		}
		return new GradeRewardHistory(
				rewardId, memberUuid, gradeId, rewardMonth, tokenAmount, RewardStatus.CANCELED, createdAt
		);
	}

	public Long rewardId() {
		return rewardId;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public Long gradeId() {
		return gradeId;
	}

	public String rewardMonth() {
		return rewardMonth;
	}

	public long tokenAmount() {
		return tokenAmount;
	}

	public RewardStatus rewardStatus() {
		return rewardStatus;
	}

	public LocalDateTime createdAt() {
		return createdAt;
	}

	private static String requireRewardMonth(String rewardMonth) {
		String trimmed = trimToNull(rewardMonth);
		if (trimmed == null || !REWARD_MONTH_PATTERN.matcher(trimmed).matches()) {
			throw new InvalidGradeException("Reward month must be in yyyy-MM format.");
		}
		return trimmed;
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
