package com.planwith.planwith_fo_grade.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_grade.domain.exception.InvalidGradeException;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class MemberGradeMetricTest {

	private final MemberUuid memberUuid = MemberUuid.from(UUID.randomUUID().toString());
	private final LocalDateTime syncedAt = LocalDateTime.of(2026, 8, 18, 10, 0);

	@Test
	void initializesMetricWithZeroValue() {
		MemberGradeMetric metric = MemberGradeMetric.initialize(
				memberUuid, MemberMetricType.STORY_COUNT, "story-service", syncedAt
		);

		assertThat(metric.metricId()).isNull();
		assertThat(metric.currentValue()).isZero();
		assertThat(metric.sourceVersion()).isZero();
	}

	@Test
	void appliesDeltaToCurrentValue() {
		MemberGradeMetric metric = MemberGradeMetric.initialize(
				memberUuid, MemberMetricType.STORY_COUNT, "story-service", syncedAt
		);

		MemberGradeMetric updated = metric.applyDelta(3L, "story-service", 1L, syncedAt.plusMinutes(1));

		assertThat(updated.currentValue()).isEqualTo(3L);
		assertThat(updated.sourceVersion()).isEqualTo(1L);
	}

	@Test
	void increasesAndDecreasesStoryFollowAndLikeMetrics() {
		MemberGradeMetric story = metric(MemberMetricType.STORY_COUNT, 10L, "story-service", 1L);
		MemberGradeMetric follower = metric(MemberMetricType.FOLLOWER_COUNT, 99L, "follow-service", 1L);
		MemberGradeMetric like = metric(MemberMetricType.RECEIVED_LIKE_COUNT, 499L, "like-service", 1L);

		assertThat(story.applyDelta(1L, "story-service", 2L, syncedAt.plusMinutes(1)).currentValue()).isEqualTo(11L);
		assertThat(story.applyDelta(1L, "story-service", 2L, syncedAt.plusMinutes(1))
				.applyDelta(-1L, "story-service", 3L, syncedAt.plusMinutes(2)).currentValue()).isEqualTo(10L);
		assertThat(follower.applyDelta(1L, "follow-service", 2L, syncedAt.plusMinutes(1)).currentValue()).isEqualTo(100L);
		assertThat(follower.applyDelta(1L, "follow-service", 2L, syncedAt.plusMinutes(1))
				.applyDelta(-1L, "follow-service", 3L, syncedAt.plusMinutes(2)).currentValue()).isEqualTo(99L);
		assertThat(like.applyDelta(1L, "like-service", 2L, syncedAt.plusMinutes(1)).currentValue()).isEqualTo(500L);
		assertThat(like.applyDelta(1L, "like-service", 2L, syncedAt.plusMinutes(1))
				.applyDelta(-1L, "like-service", 3L, syncedAt.plusMinutes(2)).currentValue()).isEqualTo(499L);
	}

	@Test
	void doesNotAllowMetricValueBelowZero() {
		MemberGradeMetric metric = MemberGradeMetric.initialize(
				memberUuid, MemberMetricType.STORY_COUNT, "story-service", syncedAt
		);

		assertThatThrownBy(() -> metric.applyDelta(-1L, "story-service", 1L, syncedAt.plusMinutes(1)))
				.isInstanceOf(InvalidGradeException.class);
	}

	@Test
	void ignoresDuplicateSourceVersion() {
		MemberGradeMetric metric = MemberGradeMetric.reconstitute(
				1L, memberUuid, MemberMetricType.STORY_COUNT, 10L, "story-service", 5L, syncedAt
		);

		MemberGradeMetric duplicate = metric.synchronize(99L, "story-service", 5L, syncedAt.plusMinutes(1));

		assertThat(duplicate).isSameAs(metric);
		assertThat(duplicate.currentValue()).isEqualTo(10L);
	}

	@Test
	void ignoresStaleSourceVersion() {
		MemberGradeMetric metric = MemberGradeMetric.reconstitute(
				1L, memberUuid, MemberMetricType.STORY_COUNT, 10L, "story-service", 5L, syncedAt
		);

		MemberGradeMetric stale = metric.synchronize(99L, "story-service", 4L, syncedAt.plusMinutes(1));

		assertThat(stale).isSameAs(metric);
		assertThat(stale.currentValue()).isEqualTo(10L);
	}

	@Test
	void memberMetricTypeIncludesPostCountButGradeMetricTypeDoesNot() {
		assertThat(MemberMetricType.values()).contains(MemberMetricType.POST_COUNT);
		assertThat(GradeMetricType.values()).doesNotContainNull();
		assertThat(MemberMetricType.fromGradeMetricType(GradeMetricType.STORY_COUNT))
				.isEqualTo(MemberMetricType.STORY_COUNT);
		assertThat(MemberMetricType.STORY_COUNT.toGradeMetricType()).contains(GradeMetricType.STORY_COUNT);
		assertThat(MemberMetricType.POST_COUNT.toGradeMetricType()).isEmpty();
	}

	@Test
	void rejectsNegativeSynchronizedValue() {
		MemberGradeMetric metric = MemberGradeMetric.initialize(
				memberUuid, MemberMetricType.POST_COUNT, "story-service", syncedAt
		);

		assertThatThrownBy(() -> metric.synchronize(-1L, "story-service", 1L, syncedAt.plusMinutes(1)))
				.isInstanceOf(InvalidGradeException.class);
	}

	private MemberGradeMetric metric(
			MemberMetricType metricType,
			long currentValue,
			String sourceService,
			long sourceVersion
	) {
		return MemberGradeMetric.reconstitute(
				1L,
				memberUuid,
				metricType,
				currentValue,
				sourceService,
				sourceVersion,
				syncedAt
		);
	}
}
