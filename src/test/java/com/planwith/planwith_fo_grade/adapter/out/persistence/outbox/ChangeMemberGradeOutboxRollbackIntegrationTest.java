package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_grade.application.GradeCriteriaInitializer;
import com.planwith.planwith_fo_grade.application.command.ChangeMemberGradeCommand;
import com.planwith.planwith_fo_grade.application.port.in.ChangeMemberGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.GradeCriteriaPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeMemberPort;
import com.planwith.planwith_fo_grade.domain.model.GradeCode;
import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
class ChangeMemberGradeOutboxRollbackIntegrationTest {

	@Autowired
	private ChangeMemberGradeUseCase changeMemberGradeUseCase;

	@Autowired
	private GradeCriteriaPort gradeCriteriaPort;

	@Autowired
	private GradeMemberPort gradeMemberPort;

	@Autowired
	private SpringDataGradeOutboxRepository repository;

	@Test
	void rollsBackGradeMemberWhenOutboxInsertIsFollowedByFailure() {
		new GradeCriteriaInitializer(gradeCriteriaPort).run(new DefaultApplicationArguments());
		String memberUuid = UUID.randomUUID().toString();
		LocalDateTime assignedAt = LocalDateTime.of(2026, 8, 19, 3, 0);
		gradeMemberPort.save(GradeMember.assign(
				gradeCriteriaPort.findByGradeCode(GradeCode.TRAVELER).orElseThrow().gradeId(),
				MemberUuid.from(memberUuid),
				assignedAt
		));

		assertThatThrownBy(() -> changeMemberGradeUseCase.change(new ChangeMemberGradeCommand(
				memberUuid,
				GradeCode.TRAVELER.name(),
				GradeCode.EXPLORER.name()
		))).isInstanceOf(IllegalStateException.class)
				.hasMessage("Outbox INSERT 이후 강제 실패");

		GradeMember saved = gradeMemberPort.findByMemberUuid(MemberUuid.from(memberUuid)).orElseThrow();
		assertThat(saved.gradeId()).isEqualTo(
				gradeCriteriaPort.findByGradeCode(GradeCode.TRAVELER).orElseThrow().gradeId()
		);
		assertThat(repository.findAll()).noneMatch(outbox ->
				UUID.fromString(memberUuid).equals(outbox.aggregateUuid())
		);
		assertThat(repository.findUnpublished(PageRequest.of(0, 10))).noneMatch(outbox ->
				UUID.fromString(memberUuid).equals(outbox.aggregateUuid())
		);
	}

	@TestConfiguration
	static class FailingOutboxAfterInsertConfig {

		@Bean
		@Primary
		GradeEventOutboxPort failingOutboxAfterInsert(SpringDataGradeOutboxRepository repository) {
			GradeEventOutboxAdapter delegate = new GradeEventOutboxAdapter(repository);
			return message -> {
				delegate.save(message);
				throw new IllegalStateException("Outbox INSERT 이후 강제 실패");
			};
		}
	}
}
