package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.event.GradeChangedEvent;
import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeOutboxMessage;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradeEventOutboxAdapterIntegrationTest {

	@Autowired
	private GradeEventOutboxPort outboxPort;

	@Autowired
	private SpringDataGradeOutboxRepository repository;

	@BeforeEach
	void clearOutbox() {
		repository.deleteAll();
	}

	@Test
	void storesOnlyOneOutboxRecordForSameEventUuid() {
		String eventUuid = UUID.randomUUID().toString();
		GradeOutboxMessage message = new GradeOutboxMessage(
				eventUuid,
				"Grade",
				UUID.randomUUID().toString(),
				GradeChangedEvent.EVENT_TYPE,
				"{\"memberUuid\":\"member-uuid\"}"
		);

		outboxPort.save(message);
		outboxPort.save(message);

		assertThat(repository.findAll()).singleElement().satisfies(outbox -> {
			assertThat(outbox.eventUuid()).isEqualTo(UUID.fromString(eventUuid));
			assertThat(outbox.eventType()).isEqualTo(GradeChangedEvent.EVENT_TYPE);
			assertThat(outbox.payload()).contains("memberUuid");
			assertThat(outbox.publishedAt()).isNull();
			assertThat(outbox.retryCount()).isZero();
		});
		assertThat(repository.findUnpublished(PageRequest.of(0, 10))).hasSize(1);
	}
}
