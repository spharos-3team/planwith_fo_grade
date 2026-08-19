package com.planwith.planwith_fo_grade.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.command.AssignInitialGradeCommand;
import com.planwith.planwith_fo_grade.application.port.in.AssignInitialGradeUseCase;

class MemberCreatedEventConsumerTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void assignsInitialGradeWhenMemberUuidIsPresent() {
		CapturingAssignInitialGradeUseCase useCase = new CapturingAssignInitialGradeUseCase();
		MemberCreatedEventConsumer consumer = new MemberCreatedEventConsumer(useCase, objectMapper);
		String memberUuid = UUID.randomUUID().toString();
		String payload = """
				{"eventUuid":"%s","memberUuid":"%s","eventType":"MemberCreated"}
				""".formatted(UUID.randomUUID(), memberUuid);

		consumer.consume("planwith.member.created", payload);

		assertThat(useCase.commands).hasSize(1);
		assertThat(useCase.commands.get(0).memberUuid()).isEqualTo(memberUuid);
		assertThat(useCase.commands.get(0).assignedAt()).isInstanceOf(LocalDateTime.class);
	}

	@Test
	void ignoresInvalidPayload() {
		CapturingAssignInitialGradeUseCase useCase = new CapturingAssignInitialGradeUseCase();
		MemberCreatedEventConsumer consumer = new MemberCreatedEventConsumer(useCase, objectMapper);

		consumer.consume("planwith.member.created", "{not-json");
		consumer.consume("planwith.member.created", "{\"eventUuid\":\"%s\"}".formatted(UUID.randomUUID()));

		assertThat(useCase.commands).isEmpty();
	}

	private static final class CapturingAssignInitialGradeUseCase implements AssignInitialGradeUseCase {

		private final List<AssignInitialGradeCommand> commands = new ArrayList<>();

		@Override
		public void assign(AssignInitialGradeCommand command) {
			commands.add(command);
		}
	}
}
