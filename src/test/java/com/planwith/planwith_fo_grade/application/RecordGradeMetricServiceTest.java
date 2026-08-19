package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import com.planwith.planwith_fo_grade.application.command.EvaluateGradeCommand;
import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;
import com.planwith.planwith_fo_grade.application.port.in.EvaluateGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.application.port.out.ProcessedGradeEventPort;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.ProcessedGradeEvent;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class RecordGradeMetricServiceTest {

	private final String memberUuid = UUID.randomUUID().toString();

	@Test
	void increasesStoryCountFromZero() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		RecordGradeMetricService service = service(port, new InMemoryProcessedGradeEventPort());

		service.record(command(MemberMetricType.STORY_COUNT.name(), 1L));

		MemberGradeMetric saved = port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow();
		assertThat(saved.currentValue()).isEqualTo(1L);
		assertThat(saved.sourceService()).isEqualTo("story-service");
		assertThat(saved.sourceVersion()).isEqualTo(1L);
	}

	@Test
	void decreasesMetricAndDoesNotGoBelowZero() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		RecordGradeMetricService service = service(port, new InMemoryProcessedGradeEventPort());
		service.record(command(MemberMetricType.FOLLOWER_COUNT.name(), 1L));

		service.record(command(MemberMetricType.FOLLOWER_COUNT.name(), -1L));
		service.record(command(MemberMetricType.FOLLOWER_COUNT.name(), -1L));

		MemberGradeMetric saved = port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.FOLLOWER_COUNT
		).orElseThrow();
		assertThat(saved.currentValue()).isZero();
		assertThat(saved.sourceService()).isEqualTo("follow-service");
	}

	@Test
	void ignoresDuplicateEventUuid() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		RecordGradeMetricService service = service(port, new InMemoryProcessedGradeEventPort());
		String eventUuid = UUID.randomUUID().toString();

		service.record(command(eventUuid, MemberMetricType.STORY_COUNT.name(), 1L, null));
		service.record(command(eventUuid, MemberMetricType.STORY_COUNT.name(), 1L, null));

		assertThat(port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow().currentValue()).isEqualTo(1L);
	}

	@Test
	void ignoresDuplicateSourceVersion() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		RecordGradeMetricService service = service(port, new InMemoryProcessedGradeEventPort());

		service.record(command(UUID.randomUUID().toString(), MemberMetricType.STORY_COUNT.name(), 1L, 15L));
		service.record(command(UUID.randomUUID().toString(), MemberMetricType.STORY_COUNT.name(), 1L, 15L));

		assertThat(port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow().currentValue()).isEqualTo(1L);
	}

	@Test
	void ignoresOlderSourceVersion() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		RecordGradeMetricService service = service(port, new InMemoryProcessedGradeEventPort());

		service.record(command(UUID.randomUUID().toString(), MemberMetricType.STORY_COUNT.name(), 1L, 16L));
		service.record(command(UUID.randomUUID().toString(), MemberMetricType.STORY_COUNT.name(), 1L, 15L));

		MemberGradeMetric saved = port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow();
		assertThat(saved.currentValue()).isEqualTo(1L);
		assertThat(saved.sourceVersion()).isEqualTo(16L);
	}

	@Test
	void triggersGradeEvaluationAfterMetricUpdate() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		EvaluateGradeUseCase evaluateGradeUseCase = mock(EvaluateGradeUseCase.class);
		RecordGradeMetricService service = new RecordGradeMetricService(
				port,
				new InMemoryProcessedGradeEventPort(),
				evaluateProvider(evaluateGradeUseCase)
		);

		service.record(command(MemberMetricType.RECEIVED_LIKE_COUNT.name(), 1L));

		verify(evaluateGradeUseCase).evaluate(new EvaluateGradeCommand(memberUuid));
		assertThat(port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.RECEIVED_LIKE_COUNT
		).orElseThrow().currentValue()).isEqualTo(1L);
	}

	@Test
	void doesNotReevaluateWhenDuplicateEventIsIgnored() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		EvaluateGradeUseCase evaluateGradeUseCase = mock(EvaluateGradeUseCase.class);
		RecordGradeMetricService service = new RecordGradeMetricService(
				port,
				new InMemoryProcessedGradeEventPort(),
				evaluateProvider(evaluateGradeUseCase)
		);
		String eventUuid = UUID.randomUUID().toString();

		service.record(command(eventUuid, MemberMetricType.STORY_COUNT.name(), 1L, null));
		service.record(command(eventUuid, MemberMetricType.STORY_COUNT.name(), 1L, null));

		verify(evaluateGradeUseCase, times(1)).evaluate(new EvaluateGradeCommand(memberUuid));
	}

	@Test
	void keepsMetricUpdateWhenGradeEvaluationFails() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		EvaluateGradeUseCase evaluateGradeUseCase = mock(EvaluateGradeUseCase.class);
		doThrow(new RuntimeException("evaluation unavailable"))
				.when(evaluateGradeUseCase).evaluate(new EvaluateGradeCommand(memberUuid));
		RecordGradeMetricService service = new RecordGradeMetricService(
				port,
				new InMemoryProcessedGradeEventPort(),
				evaluateProvider(evaluateGradeUseCase)
		);

		service.record(command(MemberMetricType.STORY_COUNT.name(), 1L));

		assertThat(port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow().currentValue()).isEqualTo(1L);
	}

	private RecordGradeMetricService service(
			MemberGradeMetricPort port,
			ProcessedGradeEventPort processedGradeEventPort
	) {
		return new RecordGradeMetricService(port, processedGradeEventPort, emptyEvaluation());
	}

	private RecordGradeMetricCommand command(String metricType, long delta) {
		return command(UUID.randomUUID().toString(), metricType, delta, null);
	}

	private RecordGradeMetricCommand command(String eventUuid, String metricType, long delta, Long sourceVersion) {
		return new RecordGradeMetricCommand(eventUuid, memberUuid, metricType, delta, sourceVersion);
	}

	@SuppressWarnings("unchecked")
	private static ObjectProvider<EvaluateGradeUseCase> evaluateProvider(EvaluateGradeUseCase useCase) {
		ObjectProvider<EvaluateGradeUseCase> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(useCase);
		return provider;
	}

	@SuppressWarnings("unchecked")
	private static ObjectProvider<EvaluateGradeUseCase> emptyEvaluation() {
		ObjectProvider<EvaluateGradeUseCase> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(null);
		return provider;
	}

	private static final class InMemoryMemberGradeMetricPort implements MemberGradeMetricPort {

		private final Map<String, MemberGradeMetric> metrics = new LinkedHashMap<>();

		@Override
		public Optional<MemberGradeMetric> findByMemberUuidAndMetricType(
				MemberUuid memberUuid,
				MemberMetricType metricType
		) {
			return Optional.ofNullable(metrics.get(key(memberUuid, metricType)));
		}

		@Override
		public List<MemberGradeMetric> findByMemberUuid(MemberUuid memberUuid) {
			return metrics.values().stream()
					.filter(metric -> metric.memberUuid().equals(memberUuid))
					.toList();
		}

		@Override
		public MemberGradeMetric save(MemberGradeMetric metric) {
			metrics.put(key(metric.memberUuid(), metric.metricType()), metric);
			return metric;
		}

		private static String key(MemberUuid memberUuid, MemberMetricType metricType) {
			return memberUuid + ":" + metricType;
		}
	}

	private static final class InMemoryProcessedGradeEventPort implements ProcessedGradeEventPort {

		private final Set<UUID> processed = new LinkedHashSet<>();

		@Override
		public boolean existsByEventUuid(UUID eventUuid) {
			return processed.contains(eventUuid);
		}

		@Override
		public void save(ProcessedGradeEvent event) {
			processed.add(event.eventUuid());
		}
	}
}
