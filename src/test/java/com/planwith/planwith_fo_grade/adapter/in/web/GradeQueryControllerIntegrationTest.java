package com.planwith.planwith_fo_grade.adapter.in.web;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GradeQueryControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private MemberGradeMetricPort memberGradeMetricPort;

	@Test
	void listsAllGradesFromDatabase() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());

		mockMvc.perform(get("/api/grade/grades"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(6))
				.andExpect(jsonPath("$.data[0].gradeCode").value("ROOKIE"))
				.andExpect(jsonPath("$.data[0].gradeName").value("🌱 새싹"))
				.andExpect(jsonPath("$.data[0].gradeLevel").value(1))
				.andExpect(jsonPath("$.data[0].conditions.length()").value(0))
				.andExpect(jsonPath("$.data[0].benefits[0].benefitCode").value("MONTHLY_FREE_TOKEN"))
				.andExpect(jsonPath("$.data[2].gradeCode").value("TRAVELER"))
				.andExpect(jsonPath("$.data[2].conditions[0].metricType").value("STORY_COUNT"))
				.andExpect(jsonPath("$.data[2].conditions[0].thresholdValue").value(10))
				.andExpect(jsonPath("$.data[2].conditions[1].thresholdValue").value(100))
				.andExpect(jsonPath("$.data[2].conditions[2].thresholdValue").value(500))
				.andExpect(jsonPath("$.data[3].gradeCode").value("EXPLORER"))
				.andExpect(jsonPath("$.data[3].gradeLevel").value(4));
	}

	@Test
	void returnsMyGradeManagementForLeafMember() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.LEAF).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		saveMetric(memberUuid, MemberMetricType.STORY_COUNT, 7L, "story-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.FOLLOWER_COUNT, 62L, "follow-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT, 410L, "like-service", assignedAt);

		mockMvc.perform(get("/api/grade/grades/me").header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.currentGrade.code").value("LEAF"))
				.andExpect(jsonPath("$.data.currentGrade.name").value("🧳 잎새"))
				.andExpect(jsonPath("$.data.currentGrade.level").value(2))
				.andExpect(jsonPath("$.data.currentGrade.benefits[0].benefitCode").value("MONTHLY_FREE_TOKEN"))
				.andExpect(jsonPath("$.data.currentMetrics.storyCount").value(7))
				.andExpect(jsonPath("$.data.currentMetrics.followerCount").value(62))
				.andExpect(jsonPath("$.data.currentMetrics.receivedLikeCount").value(410))
				.andExpect(jsonPath("$.data.nextGrade.code").value("TRAVELER"))
				.andExpect(jsonPath("$.data.nextGrade.conditions[0].thresholdValue").value(10))
				.andExpect(jsonPath("$.data.progress.story.current").value(7))
				.andExpect(jsonPath("$.data.progress.story.required").value(10))
				.andExpect(jsonPath("$.data.progress.story.remaining").value(3))
				.andExpect(jsonPath("$.data.progress.story.percentage").value(70))
				.andExpect(jsonPath("$.data.progress.follower.remaining").value(38))
				.andExpect(jsonPath("$.data.progress.follower.percentage").value(62))
				.andExpect(jsonPath("$.data.progress.receivedLike.remaining").value(90))
				.andExpect(jsonPath("$.data.progress.receivedLike.percentage").value(82))
				.andExpect(jsonPath("$.data.currentBenefits.monthlyTokenAmount").value(20))
				.andExpect(jsonPath("$.data.currentBenefits.profileBadge").value(false))
				.andExpect(jsonPath("$.data.currentBenefits.membershipAccess").value(false));
	}

	@Test
	void returnsIntegratedGradeManagementPageForLeafMember() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.LEAF).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));
		saveMetric(memberUuid, MemberMetricType.STORY_COUNT, 7L, "story-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.FOLLOWER_COUNT, 62L, "follow-service", assignedAt);
		saveMetric(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT, 410L, "like-service", assignedAt);

		mockMvc.perform(get("/api/grade/grades/me/management").header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.grades.length()").value(6))
				.andExpect(jsonPath("$.data.grades[0].gradeCode").value("ROOKIE"))
				.andExpect(jsonPath("$.data.grades[1].gradeCode").value("LEAF"))
				.andExpect(jsonPath("$.data.grades[2].gradeCode").value("TRAVELER"))
				.andExpect(jsonPath("$.data.grades[2].conditions[0].thresholdValue").value(10))
				.andExpect(jsonPath("$.data.grades[5].gradeCode").value("PLANWITH"))
				.andExpect(jsonPath("$.data.currentGrade.code").value("LEAF"))
				.andExpect(jsonPath("$.data.currentGrade.name").value("🧳 잎새"))
				.andExpect(jsonPath("$.data.currentGrade.level").value(2))
				.andExpect(jsonPath("$.data.currentMetrics.storyCount").value(7))
				.andExpect(jsonPath("$.data.currentMetrics.followerCount").value(62))
				.andExpect(jsonPath("$.data.currentMetrics.receivedLikeCount").value(410))
				.andExpect(jsonPath("$.data.nextGrade.code").value("TRAVELER"))
				.andExpect(jsonPath("$.data.nextGrade.conditions[0].thresholdValue").value(10))
				.andExpect(jsonPath("$.data.progress.story.remaining").value(3))
				.andExpect(jsonPath("$.data.progress.story.percentage").value(70))
				.andExpect(jsonPath("$.data.progress.follower.remaining").value(38))
				.andExpect(jsonPath("$.data.progress.receivedLike.remaining").value(90))
				.andExpect(jsonPath("$.data.currentBenefits.monthlyTokenAmount").value(20))
				.andExpect(jsonPath("$.data.currentBenefits.profileBadge").value(false));
	}

	@Test
	void returnsIntegratedPageWithoutNextGradeForHighestGrade() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.PLANWITH).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));

		mockMvc.perform(get("/api/grade/grades/me/management").header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.grades.length()").value(6))
				.andExpect(jsonPath("$.data.currentGrade.code").value("PLANWITH"))
				.andExpect(jsonPath("$.data.nextGrade").value(nullValue()))
				.andExpect(jsonPath("$.data.progress.story.percentage").value(100))
				.andExpect(jsonPath("$.data.currentBenefits.monthlyTokenAmount").value(120))
				.andExpect(jsonPath("$.data.currentBenefits.membershipAccess").value(true))
				.andExpect(jsonPath("$.data.currentBenefits.storyPriorityExposure").value("HIGHEST"));
	}

	@Test
	void returnsCurrentBenefitsForExplorerMember() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));

		mockMvc.perform(get("/api/grade/grades/me/benefits").header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.gradeCode").value("EXPLORER"))
				.andExpect(jsonPath("$.data.monthlyTokenAmount").value(50))
				.andExpect(jsonPath("$.data.profileBadge").value(true))
				.andExpect(jsonPath("$.data.profileSpecialBorder").value(false))
				.andExpect(jsonPath("$.data.membershipPublicStory").value(true))
				.andExpect(jsonPath("$.data.membershipAccess").value(false))
				.andExpect(jsonPath("$.data.storyPriorityExposure").value(nullValue()));
	}

	@Test
	void returnsHighestStoryPriorityForPlanwithMember() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.PLANWITH).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));

		mockMvc.perform(get("/api/grade/grades/me/benefits").header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.monthlyTokenAmount").value(120))
				.andExpect(jsonPath("$.data.membershipAccess").value(true))
				.andExpect(jsonPath("$.data.storyPriorityExposure").value("HIGHEST"));
	}

	@Test
	void returnsNotFoundWhenMemberGradeIsMissing() throws Exception {
		mockMvc.perform(get("/api/grade/grades/me").header("X-Member-UUID", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("GRADE_NOT_FOUND"));
	}

	@Test
	void returnsNotFoundWhenIntegratedManagementMemberGradeIsMissing() throws Exception {
		mockMvc.perform(get("/api/grade/grades/me/management").header("X-Member-UUID", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("GRADE_NOT_FOUND"));
	}

	@Test
	void returnsUnauthorizedWhenMemberHeaderIsMissing() throws Exception {
		mockMvc.perform(get("/api/grade/grades/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void returnsUnauthorizedWhenIntegratedManagementHeaderIsMissing() throws Exception {
		mockMvc.perform(get("/api/grade/grades/me/management"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
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
