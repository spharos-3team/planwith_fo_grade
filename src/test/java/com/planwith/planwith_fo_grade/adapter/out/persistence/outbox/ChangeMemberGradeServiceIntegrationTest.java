package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.command.ChangeMemberGradeCommand;
import com.planwith.planwith_fo_grade.application.port.in.ChangeMemberGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.domain.event.GradeEventType;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChangeMemberGradeServiceIntegrationTest {

	@Autowired
	private ChangeMemberGradeUseCase changeMemberGradeUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private SpringDataGradeOutboxRepository repository;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void clearOutbox() {
		repository.deleteAll();
	}

	@Test
	void updatesTravelerToExplorerAndStoresUnpublishedOutbox() throws Exception {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.TRAVELER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));

		changeMemberGradeUseCase.change(new ChangeMemberGradeCommand(
				memberUuid,
				GradeCode.TRAVELER.name(),
				GradeCode.EXPLORER.name()
		));

		GradeMember saved = gradeMemberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(
				gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeId()
		);
		assertThat(saved.lastEvaluatedAt()).isNotNull();
		assertThat(repository.findUnpublished(PageRequest.of(0, 10))).singleElement().satisfies(outbox -> {
			assertThat(outbox.eventType()).isEqualTo(GradeEventType.GRADE_CHANGED.name());
			assertThat(outbox.publishedAt()).isNull();
			JsonNode payload = objectMapper.readTree(outbox.payload());
			assertThat(payload.get("memberUuid").asText()).isEqualTo(memberUuid);
			assertThat(payload.get("previousGrade").asText()).isEqualTo(GradeCode.TRAVELER.name());
			assertThat(payload.get("currentGrade").asText()).isEqualTo(GradeCode.EXPLORER.name());
			assertThat(payload.get("gradeLevel").asInt()).isEqualTo(
					gradeCriteriaPort.findByGradeCode(GradeCode.EXPLORER).orElseThrow().gradeLevel()
			);
			assertThat(payload.get("changedAt").asText()).isNotBlank();
		});
	}
}
