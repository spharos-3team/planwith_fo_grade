package com.planwith.planwith_fo_grade.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberCreatedEventPayload(
		String eventUuid,
		String memberUuid
) {
}
