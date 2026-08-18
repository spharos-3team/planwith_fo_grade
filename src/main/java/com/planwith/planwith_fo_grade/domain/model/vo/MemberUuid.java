package com.planwith.planwith_fo_grade.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record MemberUuid(UUID value) {

	public MemberUuid {
		Objects.requireNonNull(value, "Member UUID is required.");
	}

	public static MemberUuid from(String value) {
		return new MemberUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
