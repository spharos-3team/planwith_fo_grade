package com.planwith.planwith_fo_grade.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.command.GrantGradeRewardCommand;
import com.planwith.planwith_fo_grade.application.event.GradeChangedEvent;
import com.planwith.planwith_fo_grade.application.event.GradeRewardGrantedEvent;
import com.planwith.planwith_fo_grade.application.port.in.AssignInitialGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.in.GetCurrentBenefitsQueryUseCase;
import com.planwith.planwith_fo_grade.application.port.in.GrantGradeRewardUseCase;
import com.planwith.planwith_fo_grade.application.port.in.RecordGradeMetricUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeRewardHistoryPort;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.application.port.out.ProcessedGradeEventPort;
import com.planwith.planwith_fo_grade.config.GradeKafkaProperties;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.RewardStatus;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GradeEventStormingIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AssignInitialGradeUseCase assignInitialGradeUseCase;

	@Autowired
	private RecordGradeMetricUseCase recordGradeMetricUseCase;

	@Autowired
	private GetCurrentBenefitsQueryUseCase getCurrentBenefitsQueryUseCase;

	@Autowired
	private GrantGradeRewardUseCase grantGradeRewardUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private MemberGradeMetricPort memberGradeMetricPort;

	@Autowired
	private GradeRewardHistoryPort gradeRewardHistoryPort;

	@Autowired
	private ProcessedGradeEventPort processedGradeEventPort;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private GradeKafkaProperties kafkaProperties;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private MemberCreatedEventConsumer memberCreatedConsumer;
	private GradeInboundEventConsumer metricConsumer;

	@BeforeEach
	void setUp() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		memberCreatedConsumer = new MemberCreatedEventConsumer(assignInitialGradeUseCase, objectMapper);
		metricConsumer = new GradeInboundEventConsumer(recordGradeMetricUseCase, objectMapper, kafkaProperties);
	}

	@Test
	void assignsRookieWhenMemberCreatedEventIsConsumed() throws Exception {
		String memberUuid = UUID.randomUUID().toString();

		memberCreatedConsumer.consume("planwith.member.created", memberCreatedPayload(memberUuid));
		memberCreatedConsumer.consume("planwith.member.created", memberCreatedPayload(memberUuid));

		assertThat(gradeCode(memberUuid)).isEqualTo(GradeCode.ROOKIE);
		assertThat(getCurrentBenefitsQueryUseCase.get(memberUuid).monthlyTokenAmount()).isEqualTo(10);
		assertThat(gradeRewardHistoryPort.findByMemberUuidAndRewardMonth(
				MemberUuid.from(memberUuid),
				currentRewardMonth()
		).orElseThrow().tokenAmount()).isEqualTo(10L);
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from grade_reward_history where member_uuid = ?",
				Long.class,
				memberUuid
		)).isEqualTo(1L);

		JsonNode payload = objectMapper.readTree(outboxPayload(memberUuid, GradeRewardGrantedEvent.EVENT_TYPE));
		assertThat(payload.get("gradeCode").asText()).isEqualTo("ROOKIE");
		assertThat(payload.get("tokenAmount").asLong()).isEqualTo(10L);
		assertThat(payload.get("rewardType").asText()).isEqualTo("MONTHLY_FREE_TOKEN");
		assertThat(payload.get("rewardMonth").asText()).isEqualTo(currentRewardMonth());
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from grade_outbox where aggregate_uuid = ? and event_type = ?",
				Long.class,
				memberUuid,
				GradeRewardGrantedEvent.EVENT_TYPE
		)).isEqualTo(1L);
	}

	@Test
	void increasesStoryFollowAndLikeMetricsFromKafkaEvents() {
		String memberUuid = UUID.randomUUID().toString();
		String likerUuid = UUID.randomUUID().toString();
		memberCreatedConsumer.consume("planwith.member.created", memberCreatedPayload(memberUuid));

		metricConsumer.consume("planwith.story.created", storyCreatedPayload(memberUuid));
		metricConsumer.consume("planwith.follow.created", followCreatedPayload(memberUuid));
		metricConsumer.consume("planwith.like.created", likeCreatedPayload(memberUuid, likerUuid));

		assertThat(metricValue(memberUuid, MemberMetricType.STORY_COUNT)).isEqualTo(1L);
		assertThat(metricValue(memberUuid, MemberMetricType.FOLLOWER_COUNT)).isEqualTo(1L);
		assertThat(metricValue(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT)).isEqualTo(1L);
		assertThat(memberGradeMetricPort.findByMemberUuidAndMetricType(
				MemberUuid.from(likerUuid),
				MemberMetricType.RECEIVED_LIKE_COUNT
		)).isEmpty();
	}

	@Test
	void promotesRookieToLeafAndStoresGradeChangedOutbox() throws Exception {
		String memberUuid = UUID.randomUUID().toString();
		memberCreatedConsumer.consume("planwith.member.created", memberCreatedPayload(memberUuid));
		repeat(3, () -> metricConsumer.consume("planwith.story.created", storyCreatedPayload(memberUuid)));
		repeat(10, () -> metricConsumer.consume("planwith.follow.created", followCreatedPayload(memberUuid)));
		repeat(30, () -> metricConsumer.consume("planwith.like.created", likeCreatedPayload(memberUuid, UUID.randomUUID().toString())));

		GradeMember saved = gradeMemberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(gradeCriteriaPort.findByGradeCode(GradeCode.LEAF).orElseThrow().gradeId());
		assertThat(saved.lastEvaluatedAt()).isNotNull();
		assertThat(metricValue(memberUuid, MemberMetricType.STORY_COUNT)).isEqualTo(3L);
		assertThat(metricValue(memberUuid, MemberMetricType.FOLLOWER_COUNT)).isEqualTo(10L);
		assertThat(metricValue(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT)).isEqualTo(30L);

		JsonNode payload = objectMapper.readTree(outboxPayload(memberUuid, GradeChangedEvent.EVENT_TYPE));
		assertThat(payload.get("previousGradeCode").asText()).isEqualTo("ROOKIE");
		assertThat(payload.get("currentGradeCode").asText()).isEqualTo("LEAF");
		assertThat(payload.get("currentBenefits").get("monthlyTokenAmount").asInt()).isEqualTo(20);
		assertThat(payload.has("applyBadge")).isFalse();
	}

	@Test
	void doesNotDoubleCountDuplicateEventUuid() {
		String memberUuid = UUID.randomUUID().toString();
		String eventUuid = UUID.randomUUID().toString();
		memberCreatedConsumer.consume("planwith.member.created", memberCreatedPayload(memberUuid));

		String payload = storyCreatedPayload(eventUuid, memberUuid, null);
		metricConsumer.consume("planwith.story.created", payload);
		metricConsumer.consume("planwith.story.created", payload);

		assertThat(metricValue(memberUuid, MemberMetricType.STORY_COUNT)).isEqualTo(1L);
		assertThat(processedGradeEventPort.existsByEventUuid(UUID.fromString(eventUuid))).isTrue();
	}

	@Test
	void ignoresOlderSourceVersion() {
		String memberUuid = UUID.randomUUID().toString();
		memberCreatedConsumer.consume("planwith.member.created", memberCreatedPayload(memberUuid));

		metricConsumer.consume("planwith.story.created", storyCreatedPayload(UUID.randomUUID().toString(), memberUuid, 20L));
		metricConsumer.consume("planwith.story.created", storyCreatedPayload(UUID.randomUUID().toString(), memberUuid, 19L));

		assertThat(metricValue(memberUuid, MemberMetricType.STORY_COUNT)).isEqualTo(1L);
		assertThat(memberGradeMetricPort.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow().sourceVersion()).isEqualTo(20L);
	}

	@Test
	void returnsGradeManagementQueryAfterLeafPromotion() throws Exception {
		String memberUuid = UUID.randomUUID().toString();
		memberCreatedConsumer.consume("planwith.member.created", memberCreatedPayload(memberUuid));
		repeat(3, () -> metricConsumer.consume("planwith.story.created", storyCreatedPayload(memberUuid)));
		repeat(10, () -> metricConsumer.consume("planwith.follow.created", followCreatedPayload(memberUuid)));
		repeat(30, () -> metricConsumer.consume("planwith.like.created", likeCreatedPayload(memberUuid, UUID.randomUUID().toString())));

		mockMvc.perform(get("/api/grade/grades/me/management").header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.grades.length()").value(6))
				.andExpect(jsonPath("$.data.currentGrade.code").value("LEAF"))
				.andExpect(jsonPath("$.data.currentBenefits.monthlyTokenAmount").value(20))
				.andExpect(jsonPath("$.data.currentMetrics.storyCount").value(3))
				.andExpect(jsonPath("$.data.currentMetrics.followerCount").value(10))
				.andExpect(jsonPath("$.data.currentMetrics.receivedLikeCount").value(30))
				.andExpect(jsonPath("$.data.nextGrade.code").value("TRAVELER"))
				.andExpect(jsonPath("$.data.nextGrade.conditions[0].thresholdValue").value(10))
				.andExpect(jsonPath("$.data.progress.story.remaining").value(7))
				.andExpect(jsonPath("$.data.progress.follower.remaining").value(90))
				.andExpect(jsonPath("$.data.progress.receivedLike.remaining").value(470));
	}

	@Test
	void grantsExplorerMonthlyTokenOnceAndPublishesRewardGrantedContract() throws Exception {
		String memberUuid = UUID.randomUUID().toString();
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				LocalDateTime.now()
		));

		GrantGradeRewardCommand command = new GrantGradeRewardCommand(
				memberUuid,
				GradeCode.EXPLORER.name(),
				GradeRewardGrantedEvent.REWARD_TYPE_MONTHLY_FREE_TOKEN,
				"2026-08"
		);
		grantGradeRewardUseCase.grant(command);
		grantGradeRewardUseCase.grant(command);

		assertThat(gradeRewardHistoryPort.findByMemberUuidAndRewardMonth(MemberUuid.from(memberUuid), "2026-08")
				.orElseThrow()
				.tokenAmount()).isEqualTo(50L);
		assertThat(gradeRewardHistoryPort.findByMemberUuidAndRewardMonth(MemberUuid.from(memberUuid), "2026-08")
				.orElseThrow()
				.rewardStatus()).isEqualTo(RewardStatus.COMPLETED);
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from grade_reward_history where member_uuid = ? and reward_month = ?",
				Long.class,
				memberUuid,
				"2026-08"
		)).isEqualTo(1L);

		JsonNode payload = objectMapper.readTree(outboxPayload(memberUuid, GradeRewardGrantedEvent.EVENT_TYPE));
		assertThat(payload.get("tokenAmount").asLong()).isEqualTo(50L);
		assertThat(payload.get("rewardType").asText()).isEqualTo("MONTHLY_FREE_TOKEN");
		assertThat(payload.get("gradeCode").asText()).isEqualTo("EXPLORER");
	}

	@Test
	void publishesExplorerBenefitEligibilityOnGradeChangedWithoutExecutingBenefits() throws Exception {
		String memberUuid = UUID.randomUUID().toString();
		memberCreatedConsumer.consume("planwith.member.created", memberCreatedPayload(memberUuid));
		saveAbsoluteMetric(memberUuid, MemberMetricType.STORY_COUNT, 30L, "story-service");
		saveAbsoluteMetric(memberUuid, MemberMetricType.FOLLOWER_COUNT, 1_000L, "follow-service");
		saveAbsoluteMetric(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT, 5_000L, "like-service");
		metricConsumer.consume("planwith.story.created", storyCreatedPayload(memberUuid));

		assertThat(gradeCode(memberUuid)).isEqualTo(GradeCode.EXPLORER);
		JsonNode payload = objectMapper.readTree(outboxPayload(memberUuid, GradeChangedEvent.EVENT_TYPE));
		assertThat(payload.get("currentGradeCode").asText()).isEqualTo("EXPLORER");
		assertThat(payload.get("currentBenefits").get("profileBadge").asBoolean()).isTrue();
		assertThat(payload.get("currentBenefits").get("membershipPublicStory").asBoolean()).isTrue();
		assertThat(payload.get("currentBenefits").get("profileSpecialBorder").asBoolean()).isFalse();
		assertThat(payload.has("applyBadge")).isFalse();
		assertThat(payload.has("applyBorder")).isFalse();
	}

	private GradeCode gradeCode(String memberUuid) {
		GradeMember saved = gradeMemberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		return gradeCriteriaPort.findAll().stream()
				.filter(grade -> grade.gradeId().equals(saved.gradeId()))
				.findFirst()
				.orElseThrow()
				.gradeCode();
	}

	private long metricValue(String memberUuid, MemberMetricType metricType) {
		return memberGradeMetricPort.findByMemberUuidAndMetricType(MemberUuid.from(memberUuid), metricType)
				.orElseThrow()
				.currentValue();
	}

	private String outboxPayload(String memberUuid, String eventType) {
		return jdbcTemplate.queryForObject(
				"select payload from grade_outbox where aggregate_uuid = ? and event_type = ?",
				String.class,
				memberUuid,
				eventType
		);
	}

	private void saveAbsoluteMetric(String memberUuid, MemberMetricType metricType, long currentValue, String sourceService) {
		MemberUuid uuid = MemberUuid.from(memberUuid);
		var current = memberGradeMetricPort.findByMemberUuidAndMetricType(uuid, metricType)
				.orElseGet(() -> com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric.initialize(
						uuid,
						metricType,
						sourceService,
						LocalDateTime.now()
				));
		memberGradeMetricPort.save(current.synchronize(
				currentValue,
				sourceService,
				Math.max(current.sourceVersion() + 1, currentValue),
				LocalDateTime.now()
		));
	}

	private static void repeat(int times, Runnable action) {
		for (int i = 0; i < times; i++) {
			action.run();
		}
	}

	private static String currentRewardMonth() {
		return YearMonth.now(ZoneOffset.UTC).toString();
	}

	private static String memberCreatedPayload(String memberUuid) {
		return """
				{"eventUuid":"%s","memberUuid":"%s","eventType":"MemberCreated"}
				""".formatted(UUID.randomUUID(), memberUuid);
	}

	private static String storyCreatedPayload(String memberUuid) {
		return storyCreatedPayload(UUID.randomUUID().toString(), memberUuid, null);
	}

	private static String storyCreatedPayload(String eventUuid, String memberUuid, Long sourceVersion) {
		if (sourceVersion == null) {
			return """
					{"eventUuid":"%s","memberUuid":"%s","storyUuid":"%s"}
					""".formatted(eventUuid, memberUuid, UUID.randomUUID());
		}
		return """
				{"eventUuid":"%s","memberUuid":"%s","storyUuid":"%s","sourceVersion":%s}
				""".formatted(eventUuid, memberUuid, UUID.randomUUID(), sourceVersion);
	}

	private static String followCreatedPayload(String followeeUuid) {
		return """
				{"eventUuid":"%s","followerUuid":"%s","followeeUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), followeeUuid);
	}

	private static String likeCreatedPayload(String ownerUuid, String likerUuid) {
		return """
				{"eventUuid":"%s","targetType":"STORY","targetUuid":"%s","targetOwnerUuid":"%s","likerUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), ownerUuid, likerUuid);
	}
}
