package com.planwith.planwith_fo_grade.application;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.in.GetMyGradeManagementQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeQueryCachePort;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.application.query.CurrentBenefitSummaryView;
import com.planwith.planwith_fo_grade.application.query.GradeBenefitView;
import com.planwith.planwith_fo_grade.application.query.GradeConditionView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.CurrentGradeView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.CurrentMetricsView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.MetricProgressView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.NextGradeView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.ProgressView;
import com.planwith.planwith_fo_grade.domain.exception.GradeNotFoundException;
import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeBenefit;
import com.planwith.planwith_fo_grade.domain.model.GradeCondition;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_grade.domain.service.GradeProgressCalculator;
import com.planwith.planwith_fo_grade.domain.service.MetricProgress;

@Service
public class GetMyGradeManagementService implements GetMyGradeManagementQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetMyGradeManagementService.class);

	private final GradeMemberPort gradeMemberPort;
	private final GradeCriteriaPort gradeCriteriaPort;
	private final MemberGradeMetricPort memberGradeMetricPort;
	private final GradeQueryCachePort gradeQueryCachePort;
	private final GradeProgressCalculator gradeProgressCalculator = new GradeProgressCalculator();

	public GetMyGradeManagementService(
			GradeMemberPort gradeMemberPort,
			GradeCriteriaPort gradeCriteriaPort,
			MemberGradeMetricPort memberGradeMetricPort,
			GradeQueryCachePort gradeQueryCachePort
	) {
		this.gradeMemberPort = gradeMemberPort;
		this.gradeCriteriaPort = gradeCriteriaPort;
		this.memberGradeMetricPort = memberGradeMetricPort;
		this.gradeQueryCachePort = gradeQueryCachePort;
	}

	@Override
	@Transactional(readOnly = true)
	public GradeManagementView get(String memberUuidValue) {
		Objects.requireNonNull(memberUuidValue, "Member UUID is required.");
		MemberUuid memberUuid = MemberUuid.from(memberUuidValue);
		log.info("GetMyGradeManagementService : get : 내 등급 관리 조회 시작 - memberUuid={}", memberUuid);

		Optional<GradeManagementView> cached = findCached(memberUuid);
		if (cached.isPresent()) {
			log.info("GetMyGradeManagementService : get : 조회 캐시 HIT - memberUuid={}", memberUuid);
			return cached.get();
		}

		log.info("GetMyGradeManagementService : get : 조회 캐시 MISS, MySQL 조회 시작 - memberUuid={}", memberUuid);
		GradeManagementView view = loadFromDatabase(memberUuid);
		saveCache(memberUuid, view);
		log.info(
				"GetMyGradeManagementService : get : 내 등급 관리 조회 완료 - memberUuid={}, currentGradeCode={}, nextGradeCode={}",
				memberUuid,
				view.currentGrade().code(),
				view.nextGrade() == null ? null : view.nextGrade().code()
		);
		return view;
	}

	private Optional<GradeManagementView> findCached(MemberUuid memberUuid) {
		try {
			return gradeQueryCachePort.findByMemberUuid(memberUuid.toString());
		} catch (RuntimeException exception) {
			log.warn("GetMyGradeManagementService : get : Redis 장애로 MySQL 조회로 전환 - memberUuid={}", memberUuid);
			return Optional.empty();
		}
	}

	private void saveCache(MemberUuid memberUuid, GradeManagementView view) {
		try {
			gradeQueryCachePort.save(memberUuid.toString(), view);
		} catch (RuntimeException exception) {
			log.warn("GetMyGradeManagementService : get : Redis 저장 실패, MySQL 조회 결과는 유지 - memberUuid={}",
					memberUuid);
		}
	}

	private GradeManagementView loadFromDatabase(MemberUuid memberUuid) {
		GradeMember gradeMember = gradeMemberPort.findByMemberUuid(memberUuid)
				.orElseThrow(() -> new GradeNotFoundException(memberUuid.toString()));
		List<Grade> grades = gradeCriteriaPort.findAll();
		Grade currentGrade = grades.stream()
				.filter(grade -> grade.gradeId().equals(gradeMember.gradeId()))
				.findFirst()
				.orElseThrow(() -> new InvalidGradeException("Grade criteria is not configured for the member."));
		Map<GradeMetricType, Long> metricValues = toMetricValues(memberGradeMetricPort.findByMemberUuid(memberUuid));
		Optional<Grade> nextGrade = gradeProgressCalculator.nextGrade(currentGrade, grades);
		return new GradeManagementView(
				toCurrentGradeView(currentGrade),
				toCurrentMetricsView(metricValues),
				nextGrade.map(this::toNextGradeView).orElse(null),
				toProgressView(currentGrade, nextGrade.orElse(null), metricValues),
				CurrentBenefitSummaryView.from(currentGrade)
		);
	}

	private CurrentGradeView toCurrentGradeView(Grade grade) {
		return new CurrentGradeView(
				grade.gradeCode().name(),
				grade.gradeName(),
				grade.gradeLevel(),
				grade.benefits().stream().map(GetMyGradeManagementService::toBenefitView).toList()
		);
	}

	private NextGradeView toNextGradeView(Grade grade) {
		return new NextGradeView(
				grade.gradeCode().name(),
				grade.gradeName(),
				grade.conditions().stream().map(GetMyGradeManagementService::toConditionView).toList()
		);
	}

	private CurrentMetricsView toCurrentMetricsView(Map<GradeMetricType, Long> metricValues) {
		return new CurrentMetricsView(
				metricValue(metricValues, GradeMetricType.STORY_COUNT),
				metricValue(metricValues, GradeMetricType.FOLLOWER_COUNT),
				metricValue(metricValues, GradeMetricType.RECEIVED_LIKE_COUNT)
		);
	}

	private ProgressView toProgressView(
			Grade currentGrade,
			Grade nextGrade,
			Map<GradeMetricType, Long> metricValues
	) {
		if (nextGrade == null) {
			return new ProgressView(
					toCompletedView(currentGrade, metricValues, GradeMetricType.STORY_COUNT),
					toCompletedView(currentGrade, metricValues, GradeMetricType.FOLLOWER_COUNT),
					toCompletedView(currentGrade, metricValues, GradeMetricType.RECEIVED_LIKE_COUNT)
			);
		}
		return new ProgressView(
				toProgressView(nextGrade, metricValues, GradeMetricType.STORY_COUNT),
				toProgressView(nextGrade, metricValues, GradeMetricType.FOLLOWER_COUNT),
				toProgressView(nextGrade, metricValues, GradeMetricType.RECEIVED_LIKE_COUNT)
		);
	}

	private MetricProgressView toProgressView(
			Grade targetGrade,
			Map<GradeMetricType, Long> metricValues,
			GradeMetricType metricType
	) {
		return toView(gradeProgressCalculator.progress(
				metricValue(metricValues, metricType),
				gradeProgressCalculator.requiredValue(targetGrade.conditions(), metricType)
		));
	}

	private MetricProgressView toCompletedView(
			Grade currentGrade,
			Map<GradeMetricType, Long> metricValues,
			GradeMetricType metricType
	) {
		return toView(gradeProgressCalculator.completed(
				metricValue(metricValues, metricType),
				gradeProgressCalculator.requiredValue(currentGrade.conditions(), metricType)
		));
	}

	private static MetricProgressView toView(MetricProgress progress) {
		return new MetricProgressView(
				progress.current(),
				progress.required(),
				progress.remaining(),
				progress.percentage()
		);
	}

	private static GradeConditionView toConditionView(GradeCondition condition) {
		return new GradeConditionView(
				condition.metricType().name(),
				condition.conditionName(),
				condition.thresholdValue(),
				condition.description()
		);
	}

	private static GradeBenefitView toBenefitView(GradeBenefit benefit) {
		return new GradeBenefitView(
				benefit.benefitCode().name(),
				benefit.benefitName(),
				benefit.benefitValue(),
				benefit.description()
		);
	}

	private static Map<GradeMetricType, Long> toMetricValues(List<MemberGradeMetric> metrics) {
		Map<GradeMetricType, Long> values = new EnumMap<>(GradeMetricType.class);
		for (MemberGradeMetric metric : metrics) {
			metric.metricType().toGradeMetricType()
					.ifPresent(gradeMetricType -> values.put(gradeMetricType, metric.currentValue()));
		}
		return values;
	}

	private static long metricValue(Map<GradeMetricType, Long> metricValues, GradeMetricType metricType) {
		return metricValues.getOrDefault(metricType, 0L);
	}
}
