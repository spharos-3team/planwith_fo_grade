package com.planwith.planwith_fo_grade.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import com.planwith.planwith_fo_grade.application.command.EvaluateGradeCommand;
import com.planwith.planwith_fo_grade.application.command.RecordGradeMetricCommand;
import com.planwith.planwith_fo_grade.application.port.in.EvaluateGradeUseCase;
import com.planwith.planwith_fo_grade.application.port.out.MemberGradeMetricPort;
import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

class RecordGradeMetricServiceTest {

	private final String memberUuid = UUID.randomUUID().toString();

	@Test
	void increasesStoryCountFromZero() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		RecordGradeMetricService service = new RecordGradeMetricService(port, emptyEvaluation());

		service.record(new RecordGradeMetricCommand(memberUuid, MemberMetricType.STORY_COUNT.name(), 1L));

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
		RecordGradeMetricService service = new RecordGradeMetricService(port, emptyEvaluation());
		service.record(new RecordGradeMetricCommand(memberUuid, MemberMetricType.FOLLOWER_COUNT.name(), 1L));

		service.record(new RecordGradeMetricCommand(memberUuid, MemberMetricType.FOLLOWER_COUNT.name(), -1L));
		service.record(new RecordGradeMetricCommand(memberUuid, MemberMetricType.FOLLOWER_COUNT.name(), -1L));

		MemberGradeMetric saved = port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.FOLLOWER_COUNT
		).orElseThrow();
		assertThat(saved.currentValue()).isZero();
		assertThat(saved.sourceService()).isEqualTo("follow-service");
	}

	@Test
	void triggersGradeEvaluationAfterMetricUpdate() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		EvaluateGradeUseCase evaluateGradeUseCase = mock(EvaluateGradeUseCase.class);
		@SuppressWarnings("unchecked")
		ObjectProvider<EvaluateGradeUseCase> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(evaluateGradeUseCase);
		RecordGradeMetricService service = new RecordGradeMetricService(port, provider);

		service.record(new RecordGradeMetricCommand(memberUuid, MemberMetricType.RECEIVED_LIKE_COUNT.name(), 1L));

		verify(evaluateGradeUseCase).evaluate(new EvaluateGradeCommand(memberUuid));
		assertThat(port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.RECEIVED_LIKE_COUNT
		).orElseThrow().currentValue()).isEqualTo(1L);
	}

	@Test
	void keepsMetricUpdateWhenGradeEvaluationFails() {
		InMemoryMemberGradeMetricPort port = new InMemoryMemberGradeMetricPort();
		EvaluateGradeUseCase evaluateGradeUseCase = mock(EvaluateGradeUseCase.class);
		doThrow(new RuntimeException("evaluation unavailable"))
				.when(evaluateGradeUseCase).evaluate(new EvaluateGradeCommand(memberUuid));
		@SuppressWarnings("unchecked")
		ObjectProvider<EvaluateGradeUseCase> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(evaluateGradeUseCase);
		RecordGradeMetricService service = new RecordGradeMetricService(port, provider);

		service.record(new RecordGradeMetricCommand(memberUuid, MemberMetricType.STORY_COUNT.name(), 1L));

		assertThat(port.findByMemberUuidAndMetricType(
				MemberUuid.from(memberUuid),
				MemberMetricType.STORY_COUNT
		).orElseThrow().currentValue()).isEqualTo(1L);
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
		public MemberGradeMetric save(MemberGradeMetric metric) {
			metrics.put(key(metric.memberUuid(), metric.metricType()), metric);
			return metric;
		}

		private static String key(MemberUuid memberUuid, MemberMetricType metricType) {
			return memberUuid + ":" + metricType;
		}
	}
}
