package com.planwith.planwith_fo_grade.adapter.out.persistence.grade;

import java.util.List;

import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeBenefit;
import com.planwith.planwith_fo_grade.domain.model.GradeCondition;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeRewardHistory;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.RewardStatus;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

final class GradePersistenceMapper {

	private GradePersistenceMapper() {
	}

	static Grade toDomain(GradeJpaEntity entity) {
		Long gradeId = entity.getGradeId();
		List<GradeCondition> conditions = entity.getConditions().stream()
				.map(condition -> toDomainCondition(condition, gradeId))
				.toList();
		List<GradeBenefit> benefits = entity.getBenefits().stream()
				.map(benefit -> toDomainBenefit(benefit, gradeId))
				.toList();
		if (gradeId == null) {
			return Grade.create(
					entity.getGradeCode(),
					entity.getGradeName(),
					entity.getGradeLevel(),
					entity.getDescription(),
					conditions,
					benefits
			);
		}
		return Grade.reconstitute(
				gradeId,
				entity.getGradeCode(),
				entity.getGradeName(),
				entity.getGradeLevel(),
				entity.getDescription(),
				conditions,
				benefits
		);
	}

	static GradeJpaEntity toEntity(Grade grade) {
		GradeJpaEntity entity = new GradeJpaEntity();
		if (grade.gradeId() != null) {
			entity.assignGradeId(grade.gradeId());
		}
		applyToEntity(grade, entity);
		return entity;
	}

	static void applyToEntity(Grade grade, GradeJpaEntity entity) {
		entity.updateDetails(
				grade.gradeCode(),
				grade.gradeName(),
				grade.gradeLevel(),
				grade.description()
		);
		entity.replaceConditions(grade.conditions().stream()
				.map(GradePersistenceMapper::toEntityCondition)
				.toList());
		entity.replaceBenefits(grade.benefits().stream()
				.map(GradePersistenceMapper::toEntityBenefit)
				.toList());
	}

	static GradeMember toDomain(GradeMemberJpaEntity entity) {
		return GradeMember.reconstitute(
				entity.getGradeId(),
				new MemberUuid(entity.getMemberUuid()),
				entity.getGradeStatus(),
				entity.getGradeAssignedAt(),
				entity.getLastEvaluatedAt()
		);
	}

	static GradeMemberJpaEntity toEntity(GradeMember member) {
		GradeMemberJpaEntity entity = GradeMemberJpaEntity.createNew(member.memberUuid().value());
		applyToEntity(member, entity);
		return entity;
	}

	static void applyToEntity(GradeMember member, GradeMemberJpaEntity entity) {
		entity.updateDetails(
				member.gradeId(),
				member.gradeStatus(),
				member.gradeAssignedAt(),
				member.lastEvaluatedAt()
		);
	}

	static MemberGradeMetric toDomain(MemberGradeMetricJpaEntity entity) {
		MemberUuid memberUuid = new MemberUuid(entity.getMemberUuid());
		if (entity.getMetricId() == null) {
			MemberGradeMetric metric = MemberGradeMetric.initialize(
					memberUuid,
					entity.getMetricType(),
					entity.getSourceService(),
					entity.getSynchronizedAt()
			);
			return metric.synchronize(
					entity.getCurrentValue(),
					entity.getSourceService(),
					entity.getSourceVersion(),
					entity.getSynchronizedAt()
			);
		}
		return MemberGradeMetric.reconstitute(
				entity.getMetricId(),
				memberUuid,
				entity.getMetricType(),
				entity.getCurrentValue(),
				entity.getSourceService(),
				entity.getSourceVersion(),
				entity.getSynchronizedAt()
		);
	}

	static MemberGradeMetricJpaEntity toEntity(MemberGradeMetric metric) {
		MemberGradeMetricJpaEntity entity = MemberGradeMetricJpaEntity.createNew(
				metric.memberUuid().value(),
				metric.metricType()
		);
		applyToEntity(metric, entity);
		return entity;
	}

	static void applyToEntity(MemberGradeMetric metric, MemberGradeMetricJpaEntity entity) {
		entity.updateDetails(
				metric.currentValue(),
				metric.sourceService(),
				metric.sourceVersion(),
				metric.synchronizedAt()
		);
	}

	static GradeRewardHistory toDomain(GradeRewardHistoryJpaEntity entity) {
		MemberUuid memberUuid = new MemberUuid(entity.getMemberUuid());
		if (entity.getRewardId() == null) {
			if (entity.getRewardStatus() == RewardStatus.READY) {
				return GradeRewardHistory.createReady(
						memberUuid,
						entity.getGradeId(),
						entity.getRewardMonth(),
						entity.getTokenAmount(),
						entity.getCreatedAt()
				);
			}
			return GradeRewardHistory.create(
					memberUuid,
					entity.getGradeId(),
					entity.getRewardMonth(),
					entity.getTokenAmount(),
					entity.getCreatedAt()
			);
		}
		return GradeRewardHistory.reconstitute(
				entity.getRewardId(),
				memberUuid,
				entity.getGradeId(),
				entity.getRewardMonth(),
				entity.getTokenAmount(),
				entity.getRewardStatus(),
				entity.getCreatedAt()
		);
	}

	static GradeRewardHistoryJpaEntity toEntity(GradeRewardHistory history) {
		GradeRewardHistoryJpaEntity entity = GradeRewardHistoryJpaEntity.createNew(
				history.memberUuid().value(),
				history.gradeId(),
				history.rewardMonth(),
				history.createdAt()
		);
		applyToEntity(history, entity);
		return entity;
	}

	static void applyToEntity(GradeRewardHistory history, GradeRewardHistoryJpaEntity entity) {
		entity.updateDetails(history.tokenAmount(), history.rewardStatus());
	}

	private static GradeCondition toDomainCondition(GradeConditionJpaEntity entity, Long fallbackGradeId) {
		Long gradeId = entity.getGradeId() != null ? entity.getGradeId() : fallbackGradeId;
		if (entity.getConditionId() == null) {
			return GradeCondition.create(
					gradeId,
					entity.getMetricType(),
					entity.getConditionName(),
					entity.getThresholdValue(),
					entity.getSortOrder(),
					entity.getDescription()
			);
		}
		return GradeCondition.reconstitute(
				entity.getConditionId(),
				gradeId,
				entity.getMetricType(),
				entity.getConditionName(),
				entity.getThresholdValue(),
				entity.getSortOrder(),
				entity.getDescription()
		);
	}

	private static GradeBenefit toDomainBenefit(GradeBenefitJpaEntity entity, Long fallbackGradeId) {
		Long gradeId = entity.getGradeId() != null ? entity.getGradeId() : fallbackGradeId;
		if (entity.getBenefitId() == null) {
			return GradeBenefit.create(
					gradeId,
					entity.getBenefitCode(),
					entity.getBenefitName(),
					entity.getBenefitValue(),
					entity.getDescription(),
					entity.getSortOrder()
			);
		}
		return GradeBenefit.reconstitute(
				entity.getBenefitId(),
				gradeId,
				entity.getBenefitCode(),
				entity.getBenefitName(),
				entity.getBenefitValue(),
				entity.getDescription(),
				entity.getSortOrder()
		);
	}

	private static GradeConditionJpaEntity toEntityCondition(GradeCondition condition) {
		GradeConditionJpaEntity entity = new GradeConditionJpaEntity();
		entity.updateDetails(
				condition.metricType(),
				condition.conditionName(),
				condition.thresholdValue(),
				condition.sortOrder(),
				condition.description()
		);
		return entity;
	}

	private static GradeBenefitJpaEntity toEntityBenefit(GradeBenefit benefit) {
		GradeBenefitJpaEntity entity = new GradeBenefitJpaEntity();
		entity.updateDetails(
				benefit.benefitCode(),
				benefit.benefitName(),
				benefit.benefitValue(),
				benefit.description(),
				benefit.sortOrder()
		);
		return entity;
	}
}
