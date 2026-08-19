package com.planwith.planwith_fo_grade.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.port.in.GetMyGradeManagementQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradeQueryCacheAsideIntegrationTest {

	@Autowired
	private GetMyGradeManagementQueryUseCase getMyGradeManagementQueryUseCase;

	@Autowired
	private InMemoryGradeQueryCacheAdapter cache;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private MemberGradeMetricPort memberGradeMetricPort;

	@BeforeEach
	void seedCriteria() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
	}

	@Test
	void storesMysqlResultOnCacheMissThenReturnsCachedViewOnHit() {
		String memberUuid = assignedLeaf(7L, 62L, 410L);

		assertThat(cache.contains(memberUuid)).isFalse();
		GradeManagementView first = getMyGradeManagementQueryUseCase.get(memberUuid);
		assertThat(first.currentGrade().code()).isEqualTo("LEAF");
		assertThat(first.currentMetrics().storyCount()).isEqualTo(7L);
		assertThat(cache.contains(memberUuid)).isTrue();

		saveMetric(memberUuid, MemberMetricType.STORY_COUNT, 99L, "story-service", LocalDateTime.of(2026, 8, 19, 4, 0));
		GradeManagementView cached = getMyGradeManagementQueryUseCase.get(memberUuid);
		assertThat(cached.currentMetrics().storyCount()).isEqualTo(7L);

		cache.evict(memberUuid);
		GradeManagementView reloaded = getMyGradeManagementQueryUseCase.get(memberUuid);
		assertThat(reloaded.currentMetrics().storyCount()).isEqualTo(99L);
		assertThat(cache.contains(memberUuid)).isTrue();
	}

	private String assignedLeaf(long storyCount, long followerCount, long receivedLikeCount) {
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.LEAF).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		saveMetric(memberUuid, MemberMetricType.STORY_COUNT, storyCount, "story-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.FOLLOWER_COUNT, followerCount, "follow-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT, receivedLikeCount, "like-service", assignedAt);
		return memberUuid;
	}

	private void saveMetric(
			String memberUuid,
			MemberMetricType metricType,
			long currentValue,
			String sourceService,
			LocalDateTime synchronizedAt
	) {
		MemberUuid uuid = MemberUuid.from(memberUuid);
		MemberGradeMetric current = memberGradeMetricPort.findByMemberUuidAndMetricType(uuid, metricType)
				.orElseGet(() -> MemberGradeMetric.initialize(uuid, metricType, sourceService, synchronizedAt));
		memberGradeMetricPort.save(current.synchronize(
				currentValue,
				sourceService,
				current.sourceVersion() + 1,
				synchronizedAt
		));
	}
}
