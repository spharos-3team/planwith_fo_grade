package com.planwith.planwith_fo_grade.adapter.out.persistence.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataGradeOutboxRepository extends JpaRepository<GradeOutboxJpaEntity, Long> {

	boolean existsByEventUuid(UUID eventUuid);

	@Query("""
			select outbox
			from GradeOutboxJpaEntity outbox
			where outbox.publishedAt is null
			order by outbox.occurredAt asc
			""")
	List<GradeOutboxJpaEntity> findUnpublished(Pageable pageable);

	@Query("""
			select outbox
			from GradeOutboxJpaEntity outbox
			where outbox.publishedAt is null
				and (outbox.nextRetryAt is null or outbox.nextRetryAt <= :now)
			order by outbox.occurredAt asc
			""")
	List<GradeOutboxJpaEntity> findDueUnpublished(@Param("now") Instant now, Pageable pageable);
}
