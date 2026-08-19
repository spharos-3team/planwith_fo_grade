package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.MetricProgressView;
import com.planwith.planwith_fo_grade.domain.exception.GradeNotFoundException;
import com.planwith.planwith_fo_grade.domain.model.Grade;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeCriteriaCatalog;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class GetMyGradeManagementServiceTest {

	private final String memberUuid = UUID.randomUUID().toString();
	private final LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);

	@Test
	void returnsLeafProgressTowardTraveler() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		GetMyGradeManagementService service = new GetMyGradeManagementService(
				assigned(criteriaPort, GradeCode.LEAF),
				criteriaPort,
				metrics(7L, 62L, 410L)
		);

		GradeManagementView view = service.get(memberUuid);

		assertThat(view.currentGrade().code()).isEqualTo("LEAF");
		assertThat(view.currentGrade().name()).isEqualTo("🧳 잎새");
		assertThat(view.currentGrade().level()).isEqualTo(2);
		assertThat(view.currentGrade().benefits()).isNotEmpty();
		assertThat(view.currentMetrics().storyCount()).isEqualTo(7L);
		assertThat(view.currentMetrics().followerCount()).isEqualTo(62L);
		assertThat(view.currentMetrics().receivedLikeCount()).isEqualTo(410L);
		assertThat(view.nextGrade().code()).isEqualTo("TRAVELER");
		assertThat(view.nextGrade().name()).isEqualTo("✈️ 여행가");
		assertThat(view.nextGrade().conditions()).extracting(condition -> condition.thresholdValue())
				.containsExactly(10L, 100L, 500L);
		assertThat(view.progress().story()).isEqualTo(new MetricProgressView(7L, 10L, 3L, 70));
		assertThat(view.progress().follower()).isEqualTo(new MetricProgressView(62L, 100L, 38L, 62));
		assertThat(view.progress().receivedLike()).isEqualTo(new MetricProgressView(410L, 500L, 90L, 82));
		assertThat(view.currentBenefits().monthlyTokenAmount()).isEqualTo(20);
		assertThat(view.currentBenefits().profileBadge()).isFalse();
		assertThat(view.currentBenefits().membershipAccess()).isFalse();
	}

	@Test
	void returnsNullNextGradeAndCompletedProgressForHighestGrade() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		GetMyGradeManagementService service = new GetMyGradeManagementService(
				assigned(criteriaPort, GradeCode.PLANWITH),
				criteriaPort,
				metrics(180L, 40_000L, 120_000L)
		);

		GradeManagementView view = service.get(memberUuid);

		assertThat(view.currentGrade().code()).isEqualTo("PLANWITH");
		assertThat(view.nextGrade()).isNull();
		assertThat(view.progress().story()).isEqualTo(new MetricProgressView(180L, 200L, 0L, 100));
		assertThat(view.progress().follower()).isEqualTo(new MetricProgressView(40_000L, 50_000L, 0L, 100));
		assertThat(view.progress().receivedLike()).isEqualTo(new MetricProgressView(120_000L, 150_000L, 0L, 100));
	}

	@Test
	void treatsMissingMetricsAsZero() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		GetMyGradeManagementService service = new GetMyGradeManagementService(
				assigned(criteriaPort, GradeCode.ROOKIE),
				criteriaPort,
				new InMemoryMemberGradeMetricPort()
		);

		GradeManagementView view = service.get(memberUuid);

		assertThat(view.currentGrade().code()).isEqualTo("ROOKIE");
		assertThat(view.nextGrade().code()).isEqualTo("LEAF");
		assertThat(view.currentMetrics().storyCount()).isZero();
		assertThat(view.progress().story()).isEqualTo(new MetricProgressView(0L, 3L, 3L, 0));
		assertThat(view.progress().follower()).isEqualTo(new MetricProgressView(0L, 10L, 10L, 0));
		assertThat(view.progress().receivedLike()).isEqualTo(new MetricProgressView(0L, 30L, 30L, 0));
	}

	@Test
	void keepsNextGradeAsImmediateNextEvenWhenHigherGradeMetricsAreAlreadyMet() {
		InMemoryGradeCriteriaPort criteriaPort = InMemoryGradeCriteriaPort.withCatalog();
		GetMyGradeManagementService service = new GetMyGradeManagementService(
				assigned(criteriaPort, GradeCode.LEAF),
				criteriaPort,
				metrics(35L, 1_500L, 6_200L)
		);

		GradeManagementView view = service.get(memberUuid);

		assertThat(view.nextGrade().code()).isEqualTo("TRAVELER");
		assertThat(view.progress().story().remaining()).isZero();
		assertThat(view.progress().story().percentage()).isEqualTo(100);
		assertThat(view.progress().follower().percentage()).isEqualTo(100);
		assertThat(view.progress().receivedLike().percentage()).isEqualTo(100);
	}

	@Test
	void throwsWhenMemberGradeIsMissing() {
		GetMyGradeManagementService service = new GetMyGradeManagementService(
				new InMemoryGradeMemberPort(),
				InMemoryGradeCriteriaPort.withCatalog(),
				new InMemoryMemberGradeMetricPort()
		);

		assertThatThrownBy(() -> service.get(memberUuid))
				.isInstanceOf(GradeNotFoundException.class);
	}

	private InMemoryGradeMemberPort assigned(InMemoryGradeCriteriaPort criteriaPort, GradeCode gradeCode) {
		InMemoryGradeMemberPort memberPort = new InMemoryGradeMemberPort();
		memberPort.save(GradeMember.assign(
				criteriaPort.findByGradeCode(gradeCode).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		return memberPort;
	}

	private InMemoryMemberGradeMetricPort metrics(long storyCount, long followerCount, long receivedLikeCount) {
		InMemoryMemberGradeMetricPort metricPort = new InMemoryMemberGradeMetricPort();
		metricPort.save(metric(MemberMetricType.STORY_COUNT, storyCount, "story-service"));
		metricPort.save(metric(MemberMetricType.FOLLOWER_COUNT, followerCount, "follow-service"));
		metricPort.save(metric(MemberMetricType.RECEIVED_LIKE_COUNT, receivedLikeCount, "like-service"));
		return metricPort;
	}

	private MemberGradeMetric metric(MemberMetricType metricType, long currentValue, String sourceService) {
		return MemberGradeMetric.reconstitute(
				1L,
				MemberUuid.from(memberUuid),
				metricType,
				currentValue,
				sourceService,
				1L,
				assignedAt
		);
	}

	private static final class InMemoryGradeCriteriaPort implements GradeCriteriaPort {

		private final Map<GradeCode, Grade> grades = new LinkedHashMap<>();

		private static InMemoryGradeCriteriaPort withCatalog() {
			InMemoryGradeCriteriaPort port = new InMemoryGradeCriteriaPort();
			long gradeId = 1L;
			for (Grade grade : GradeCriteriaCatalog.initialGrades()) {
				port.grades.put(grade.gradeCode(), Grade.reconstitute(
						gradeId++,
						grade.gradeCode(),
						grade.gradeName(),
						grade.gradeLevel(),
						grade.description(),
						grade.conditions(),
						grade.benefits()
				));
			}
			return port;
		}

		@Override
		public List<Grade> findAll() {
			return new ArrayList<>(grades.values());
		}

		@Override
		public Optional<Grade> findByGradeCode(GradeCode gradeCode) {
			return Optional.ofNullable(grades.get(gradeCode));
		}

		@Override
		public Optional<Grade> findLowestGrade() {
			return grades.values().stream()
					.min(Comparator.comparingInt(Grade::gradeLevel));
		}

		@Override
		public Grade save(Grade grade) {
			grades.put(grade.gradeCode(), grade);
			return grade;
		}
	}

	private static final class InMemoryGradeMemberPort implements GradeMemberPort {

		private final Map<UUID, GradeMember> members = new LinkedHashMap<>();

		@Override
		public Optional<GradeMember> findByMemberUuid(MemberUuid memberUuid) {
			return Optional.ofNullable(members.get(memberUuid.value()));
		}

		@Override
		public GradeMember save(GradeMember member) {
			members.put(member.memberUuid().value(), member);
			return member;
		}
	}

	private static final class InMemoryMemberGradeMetricPort implements MemberGradeMetricPort {

		private final Map<String, MemberGradeMetric> metrics = new LinkedHashMap<>();
		private final AtomicLong ids = new AtomicLong(1L);

		@Override
		public Optional<MemberGradeMetric> findByMemberUuidAndMetricType(
				MemberUuid memberUuid,
				MemberMetricType metricType
		) {
			return Optional.ofNullable(metrics.get(key(memberUuid, metricType)));
		}

		@Override
		public List<MemberGradeMetric> findByMemberUuid(MemberUuid memberUuid) {
			return metrics.values().stream()
					.filter(metric -> metric.memberUuid().equals(memberUuid))
					.toList();
		}

		@Override
		public MemberGradeMetric save(MemberGradeMetric metric) {
			MemberGradeMetric persisted = metric.metricId() == null
					? MemberGradeMetric.reconstitute(
							ids.getAndIncrement(),
							metric.memberUuid(),
							metric.metricType(),
							metric.currentValue(),
							metric.sourceService(),
							metric.sourceVersion(),
							metric.synchronizedAt()
					)
					: metric;
			metrics.put(key(persisted.memberUuid(), persisted.metricType()), persisted);
			return persisted;
		}

		private static String key(MemberUuid memberUuid, MemberMetricType metricType) {
			return memberUuid + ":" + metricType;
		}
	}
}
