package com.planwith.planwith_fo_grade.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * FollowRemoved 수신 계약.
 * {@code followeeUuid}가 팔로워를 잃은 회원이므로 FOLLOWER_COUNT 등급 계산 대상이다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FollowRemovedEventPayload(
		String eventUuid,
		String followerUuid,
		String followeeUuid,
		String occurredAt,
		Long sourceVersion
) {
}
