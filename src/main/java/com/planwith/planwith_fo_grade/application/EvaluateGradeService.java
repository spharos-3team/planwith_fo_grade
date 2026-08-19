package com.planwith.planwith_fo_grade.application;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.command.ChangeMemberGradeCommand;
import com.planwith.planwith_fo_grade.application.command.EvaluateGradeCommand;
import com.planwith.planwith_fo_grade.application.port.in.ChangeMemberGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.in.EvaluateGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.GradeMetricType;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_grade.domain.service.GradeEvaluator;

@Service
public class EvaluateGradeService implements EvaluateGradeUseCase {

	private static final Logger log = LoggerFactory.getLogger(EvaluateGradeService.class);

	private final GradeMemberPort gradeMemberPort;
	private final GradeCriteriaPort gradeCriteriaPort;
	private final MemberGradeMetricPort memberGradeMetricPort;
	private final ChangeMemberGradeUseCase changeMemberGradeUseCase;
	private final GradeEvaluator gradeEvaluator = new GradeEvaluator();

	public EvaluateGradeService(
			GradeMemberPort gradeMemberPort,
			GradeCriteriaPort gradeCriteriaPort,
			MemberGradeMetricPort memberGradeMetricPort,
			ChangeMemberGradeUseCase changeMemberGradeUseCase
	) {
		this.gradeMemberPort = gradeMemberPort;
		this.gradeCriteriaPort = gradeCriteriaPort;
		this.memberGradeMetricPort = memberGradeMetricPort;
		this.changeMemberGradeUseCase = changeMemberGradeUseCase;
	}

	@Override
	@Transactional
	public void evaluate(EvaluateGradeCommand command) {
		Objects.requireNonNull(command, "Evaluate grade command is required.");
		MemberUuid memberUuid = MemberUuid.from(command.memberUuid());
		LocalDateTime evaluatedAt = LocalDateTime.now(ZoneOffset.UTC);

		log.info("EvaluateGradeService : evaluate : 등급 평가 시작 - memberUuid={}", memberUuid);

		Optional<GradeMember> currentMember = gradeMemberPort.findByMemberUuid(memberUuid);
		if (currentMember.isEmpty()) {
			log.warn("EvaluateGradeService : evaluate : 등급이 없는 회원이라 평가를 생략 - memberUuid={}", memberUuid);
			return;
		}
		GradeMember gradeMember = currentMember.get();
		if (!gradeMember.isActive()) {
			log.warn("EvaluateGradeService : evaluate : 활성 등급이 아니라 평가를 생략 - memberUuid={}", memberUuid);
			return;
		}

		List<Grade> grades = gradeCriteriaPort.findAll();
		Grade currentGrade = grades.stream()
				.filter(grade -> grade.gradeId().equals(gradeMember.gradeId()))
				.findFirst()
				.orElse(null);
		if (currentGrade == null) {
			log.warn("EvaluateGradeService : evaluate : 현재 등급 기준을 찾지 못해 평가를 생략 - memberUuid={}, gradeId={}",
					memberUuid, gradeMember.gradeId());
			return;
		}

		Map<GradeMetricType, Long> metricValues = toMetricValues(memberGradeMetricPort.findByMemberUuid(memberUuid));
		log.debug(
				"EvaluateGradeService : evaluate : 회원 Metric 확인 - memberUuid={}, storyCount={}, followerCount={}, receivedLikeCount={}",
				memberUuid,
				metricValues.getOrDefault(GradeMetricType.STORY_COUNT, 0L),
				metricValues.getOrDefault(GradeMetricType.FOLLOWER_COUNT, 0L),
				metricValues.getOrDefault(GradeMetricType.RECEIVED_LIKE_COUNT, 0L)
		);

		Optional<Grade> highestSatisfied = gradeEvaluator.highestSatisfiedGrade(grades, metricValues);
		if (highestSatisfied.isEmpty()) {
			log.warn("EvaluateGradeService : evaluate : 충족 등급이 없어 평가를 생략 - memberUuid={}", memberUuid);
			return;
		}

		Optional<Grade> promotion = gradeEvaluator.promotionTarget(currentGrade, highestSatisfied.get());
		if (promotion.isEmpty()) {
			gradeMemberPort.save(gradeMember.markEvaluated(evaluatedAt));
			log.info(
					"EvaluateGradeService : evaluate : 승급 조건 미충족으로 현재 등급 유지 - memberUuid={}, gradeCode={}",
					memberUuid,
					currentGrade.gradeCode()
			);
			return;
		}

		Grade promoted = promotion.get();
		changeMemberGradeUseCase.change(new ChangeMemberGradeCommand(
				memberUuid.toString(),
				currentGrade.gradeCode().name(),
				promoted.gradeCode().name()
		));
		log.info(
				"EvaluateGradeService : evaluate : 등급 승급 요청 완료 - memberUuid={}, fromGradeCode={}, toGradeCode={}",
				memberUuid,
				currentGrade.gradeCode(),
				promoted.gradeCode()
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
}
