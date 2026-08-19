package com.planwith.planwith_fo_grade.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * LikeCreated 수신 계약.
 * 등급 계산 대상은 좋아요를 누른 회원이 아니라 {@code targetOwnerUuid}(Story/Comment 작성자)이다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LikeCreatedEventPayload(
		String eventUuid,
		String targetType,
		String targetUuid,
		String targetOwnerUuid,
		String occurredAt
) {
}
