package com.planwith.planwith_fo_grade.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * StoryDeleted 수신 계약.
 * {@code memberUuid}는 스토리 작성자이며 STORY_COUNT 등급 계산 대상이다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StoryDeletedEventPayload(
		String eventUuid,
		String memberUuid,
		String storyUuid,
		String occurredAt
) {
}
