package com.planwith.planwith_fo_grade.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.FollowCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.FollowRemovedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.LikeCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.LikeRemovedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.StoryCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.StoryDeletedEventPayload;
import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;

class MetricInboundEventMapperTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void mapsStoryEventsToAuthorStoryCount() throws Exception {
		String authorUuid = UUID.randomUUID().toString();
		StoryCreatedEventPayload created = objectMapper.readValue("""
				{"eventUuid":"%s","memberUuid":"%s","storyUuid":"%s","occurredAt":"2026-08-19T04:00:00Z"}
				""".formatted(UUID.randomUUID(), authorUuid, UUID.randomUUID()), StoryCreatedEventPayload.class);
		StoryDeletedEventPayload deleted = objectMapper.readValue("""
				{"eventUuid":"%s","memberUuid":"%s","storyUuid":"%s","occurredAt":"2026-08-19T04:00:00Z"}
				""".formatted(UUID.randomUUID(), authorUuid, UUID.randomUUID()), StoryDeletedEventPayload.class);

		RecordGradeMetricCommand createdCommand = MetricInboundEventMapper.fromStoryCreated(created);
		RecordGradeMetricCommand deletedCommand = MetricInboundEventMapper.fromStoryDeleted(deleted);

		assertThat(createdCommand.memberUuid()).isEqualTo(authorUuid);
		assertThat(createdCommand.metricType()).isEqualTo(MemberMetricType.STORY_COUNT.name());
		assertThat(createdCommand.delta()).isEqualTo(1L);
		assertThat(deletedCommand.memberUuid()).isEqualTo(authorUuid);
		assertThat(deletedCommand.delta()).isEqualTo(-1L);
	}

	@Test
	void mapsFollowEventsToFolloweeFollowerCount() throws Exception {
		String followerUuid = UUID.randomUUID().toString();
		String followeeUuid = UUID.randomUUID().toString();
		FollowCreatedEventPayload created = objectMapper.readValue("""
				{"eventUuid":"%s","followerUuid":"%s","followeeUuid":"%s","occurredAt":"2026-08-19T04:00:00Z"}
				""".formatted(UUID.randomUUID(), followerUuid, followeeUuid), FollowCreatedEventPayload.class);
		FollowRemovedEventPayload removed = objectMapper.readValue("""
				{"eventUuid":"%s","followerUuid":"%s","followeeUuid":"%s","occurredAt":"2026-08-19T04:00:00Z"}
				""".formatted(UUID.randomUUID(), followerUuid, followeeUuid), FollowRemovedEventPayload.class);

		RecordGradeMetricCommand createdCommand = MetricInboundEventMapper.fromFollowCreated(created);
		RecordGradeMetricCommand removedCommand = MetricInboundEventMapper.fromFollowRemoved(removed);

		assertThat(createdCommand.memberUuid()).isEqualTo(followeeUuid);
		assertThat(createdCommand.memberUuid()).isNotEqualTo(followerUuid);
		assertThat(createdCommand.metricType()).isEqualTo(MemberMetricType.FOLLOWER_COUNT.name());
		assertThat(createdCommand.delta()).isEqualTo(1L);
		assertThat(removedCommand.memberUuid()).isEqualTo(followeeUuid);
		assertThat(removedCommand.delta()).isEqualTo(-1L);
	}

	@Test
	void mapsLikeEventsToTargetOwnerReceivedLikeCount() throws Exception {
		String likerUuid = UUID.randomUUID().toString();
		String ownerUuid = UUID.randomUUID().toString();
		LikeCreatedEventPayload created = objectMapper.readValue("""
				{
				  "eventUuid":"%s",
				  "targetType":"STORY",
				  "targetUuid":"%s",
				  "targetOwnerUuid":"%s",
				  "likerUuid":"%s",
				  "occurredAt":"2026-08-19T04:00:00Z"
				}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), ownerUuid, likerUuid), LikeCreatedEventPayload.class);
		LikeRemovedEventPayload removed = objectMapper.readValue("""
				{
				  "eventUuid":"%s",
				  "targetType":"COMMENT",
				  "targetUuid":"%s",
				  "targetOwnerUuid":"%s",
				  "occurredAt":"2026-08-19T04:00:00Z"
				}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), ownerUuid), LikeRemovedEventPayload.class);

		RecordGradeMetricCommand createdCommand = MetricInboundEventMapper.fromLikeCreated(created);
		RecordGradeMetricCommand removedCommand = MetricInboundEventMapper.fromLikeRemoved(removed);

		assertThat(createdCommand.memberUuid()).isEqualTo(ownerUuid);
		assertThat(createdCommand.memberUuid()).isNotEqualTo(likerUuid);
		assertThat(createdCommand.metricType()).isEqualTo(MemberMetricType.RECEIVED_LIKE_COUNT.name());
		assertThat(createdCommand.delta()).isEqualTo(1L);
		assertThat(removedCommand.memberUuid()).isEqualTo(ownerUuid);
		assertThat(removedCommand.delta()).isEqualTo(-1L);
	}

	@Test
	void rejectsLikeEventWithoutTargetOwnerUuid() throws Exception {
		LikeCreatedEventPayload payload = objectMapper.readValue("""
				{"eventUuid":"%s","targetType":"STORY","targetUuid":"%s","likerUuid":"%s"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()), LikeCreatedEventPayload.class);

		assertThatThrownBy(() -> MetricInboundEventMapper.fromLikeCreated(payload))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
