package com.planwith.planwith_fo_grade.adapter.in.kafka;

import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.FollowCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.FollowRemovedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.LikeCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.LikeRemovedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.StoryCreatedEventPayload;
import com.planwith.planwith_fo_grade.adapter.in.kafka.dto.StoryDeletedEventPayload;
import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;
import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommandFactory;
import com.planwith.planwith_fo_grade.domain.event.MetricEventType;

public final class MetricInboundEventMapper {

	private MetricInboundEventMapper() {
	}

	public static RecordGradeMetricCommand fromStoryCreated(StoryCreatedEventPayload payload) {
		requirePayload(payload);
		return RecordGradeMetricCommandFactory.from(
				MetricEventType.STORY_CREATED,
				payload.memberUuid(),
				payload.eventUuid(),
				payload.sourceVersion()
		);
	}

	public static RecordGradeMetricCommand fromStoryDeleted(StoryDeletedEventPayload payload) {
		requirePayload(payload);
		return RecordGradeMetricCommandFactory.from(
				MetricEventType.STORY_DELETED,
				payload.memberUuid(),
				payload.eventUuid(),
				payload.sourceVersion()
		);
	}

	public static RecordGradeMetricCommand fromFollowCreated(FollowCreatedEventPayload payload) {
		requirePayload(payload);
		return RecordGradeMetricCommandFactory.from(
				MetricEventType.FOLLOW_CREATED,
				payload.followeeUuid(),
				payload.eventUuid(),
				payload.sourceVersion()
		);
	}

	public static RecordGradeMetricCommand fromFollowRemoved(FollowRemovedEventPayload payload) {
		requirePayload(payload);
		return RecordGradeMetricCommandFactory.from(
				MetricEventType.FOLLOW_REMOVED,
				payload.followeeUuid(),
				payload.eventUuid(),
				payload.sourceVersion()
		);
	}

	public static RecordGradeMetricCommand fromLikeCreated(LikeCreatedEventPayload payload) {
		requirePayload(payload);
		return RecordGradeMetricCommandFactory.from(
				MetricEventType.LIKE_CREATED,
				payload.targetOwnerUuid(),
				payload.eventUuid(),
				payload.sourceVersion()
		);
	}

	public static RecordGradeMetricCommand fromLikeRemoved(LikeRemovedEventPayload payload) {
		requirePayload(payload);
		return RecordGradeMetricCommandFactory.from(
				MetricEventType.LIKE_REMOVED,
				payload.targetOwnerUuid(),
				payload.eventUuid(),
				payload.sourceVersion()
		);
	}

	private static void requirePayload(Object payload) {
		if (payload == null) {
			throw new IllegalArgumentException("Metric event payload is required.");
		}
	}
}
