package com.planwith.planwith_fo_grade.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;
import com.planwith.planwith_fo_grade.application.port.in.RecordGradeMetricUseCase;
import com.planwith.planwith_fo_grade.config.GradeKafkaProperties;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;

class GradeInboundEventConsumerTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final GradeKafkaProperties kafkaProperties = new GradeKafkaProperties();

	@Test
	void recordsStoryFollowAndLikeMetricsFromTopics() {
		CapturingRecordGradeMetricUseCase useCase = new CapturingRecordGradeMetricUseCase();
		GradeInboundEventConsumer consumer = new GradeInboundEventConsumer(useCase, objectMapper, kafkaProperties);
		String authorUuid = UUID.randomUUID().toString();
		String followeeUuid = UUID.randomUUID().toString();
		String ownerUuid = UUID.randomUUID().toString();

		consumer.consume("planwith.story.created", """
				{"eventUuid":"%s","memberUuid":"%s","storyUuid":"%s"}
				""".formatted(UUID.randomUUID(), authorUuid, UUID.randomUUID()));
		consumer.consume("planwith.story.deleted", """
				{"eventUuid":"%s","memberUuid":"%s","storyUuid":"%s"}
				""".formatted(UUID.randomUUID(), authorUuid, UUID.randomUUID()));
		consumer.consume("planwith.follow.created", """
				{"eventUuid":"%s","followerUuid":"%s","followeeUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), followeeUuid));
		consumer.consume("planwith.follow.removed", """
				{"eventUuid":"%s","followerUuid":"%s","followeeUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), followeeUuid));
		consumer.consume("planwith.like.created", """
				{"eventUuid":"%s","targetType":"STORY","targetUuid":"%s","targetOwnerUuid":"%s","likerUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), ownerUuid, UUID.randomUUID()));
		consumer.consume("planwith.like.removed", """
				{"eventUuid":"%s","targetType":"COMMENT","targetUuid":"%s","targetOwnerUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), ownerUuid));

		assertThat(useCase.commands).extracting(RecordGradeMetricCommand::metricType).containsExactly(
				MemberMetricType.STORY_COUNT.name(),
				MemberMetricType.STORY_COUNT.name(),
				MemberMetricType.FOLLOWER_COUNT.name(),
				MemberMetricType.FOLLOWER_COUNT.name(),
				MemberMetricType.RECEIVED_LIKE_COUNT.name(),
				MemberMetricType.RECEIVED_LIKE_COUNT.name()
		);
		assertThat(useCase.commands).extracting(RecordGradeMetricCommand::delta)
				.containsExactly(1L, -1L, 1L, -1L, 1L, -1L);
		assertThat(useCase.commands.get(0).memberUuid()).isEqualTo(authorUuid);
		assertThat(useCase.commands.get(0).eventUuid()).isNotBlank();
		assertThat(useCase.commands.get(2).memberUuid()).isEqualTo(followeeUuid);
		assertThat(useCase.commands.get(4).memberUuid()).isEqualTo(ownerUuid);
	}

	@Test
	void ignoresInvalidPayload() {
		CapturingRecordGradeMetricUseCase useCase = new CapturingRecordGradeMetricUseCase();
		GradeInboundEventConsumer consumer = new GradeInboundEventConsumer(useCase, objectMapper, kafkaProperties);

		consumer.consume("planwith.story.created", "{not-json");
		consumer.consume("planwith.like.created", """
				{"eventUuid":"%s","targetType":"STORY","targetUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID()));
		consumer.consume("planwith.story.created", """
				{"memberUuid":"%s","storyUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID()));

		assertThat(useCase.commands).isEmpty();
	}

	private static final class CapturingRecordGradeMetricUseCase implements RecordGradeMetricUseCase {

		private final List<RecordGradeMetricCommand> commands = new ArrayList<>();

		@Override
		public void record(RecordGradeMetricCommand command) {
			commands.add(command);
		}
	}
}
