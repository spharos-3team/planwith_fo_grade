package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_grade.application.port.out.GradeEventOutboxPort;
import com.planwith.planwith_fo_grade.application.port.out.GradeOutboxMessage;

@Component
public class GradeEventOutboxAdapter implements GradeEventOutboxPort {

	private static final Logger log = LoggerFactory.getLogger(GradeEventOutboxAdapter.class);

	private final SpringDataGradeOutboxRepository repository;

	public GradeEventOutboxAdapter(SpringDataGradeOutboxRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void save(GradeOutboxMessage message) {
		UUID eventUuid = UUID.fromString(message.eventUuid());
		if (repository.existsByEventUuid(eventUuid)) {
			log.warn("GradeEventOutboxAdapter : save : 중복 Outbox 이벤트 저장 생략 - eventUuid={}",
					message.eventUuid());
			return;
		}
		repository.save(new GradeOutboxJpaEntity(
				eventUuid,
				message.aggregateType(),
				UUID.fromString(message.aggregateUuid()),
				message.eventType(),
				message.payload(),
				Instant.now()
		));
		log.info("GradeEventOutboxAdapter : save : 등급 Outbox 저장 완료 - eventUuid={}, eventType={}",
				message.eventUuid(), message.eventType());
	}
}
