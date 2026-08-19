package com.planwith.planwith_fo_grade.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.query.CurrentBenefitSummaryView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.CurrentGradeView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.CurrentMetricsView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.MetricProgressView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.NextGradeView;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView.ProgressView;
import com.planwith.planwith_fo_grade.config.GradeCacheProperties;

class RedisGradeQueryCacheAdapterTest {

	private final String memberUuid = UUID.randomUUID().toString();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final GradeCacheProperties properties = new GradeCacheProperties();

	private StringRedisTemplate redisTemplate;
	private ValueOperations<String, String> valueOperations;
	private RedisGradeQueryCacheAdapter adapter;

	@BeforeEach
	void setUp() {
		redisTemplate = mock(StringRedisTemplate.class);
		valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		adapter = new RedisGradeQueryCacheAdapter(redisTemplate, objectMapper, properties);
	}

	@Test
	void returnsCachedGradeManagementViewOnHit() throws Exception {
		GradeManagementView view = leafView();
		when(valueOperations.get(properties.memberKey(memberUuid)))
				.thenReturn(objectMapper.writeValueAsString(view));

		assertThat(adapter.findByMemberUuid(memberUuid)).contains(view);
	}

	@Test
	void returnsEmptyOnMiss() {
		when(valueOperations.get(properties.memberKey(memberUuid))).thenReturn(null);

		assertThat(adapter.findByMemberUuid(memberUuid)).isEmpty();
	}

	@Test
	void returnsEmptyWhenRedisReadFailsSoMysqlCanBeUsed() {
		when(valueOperations.get(properties.memberKey(memberUuid)))
				.thenThrow(new RuntimeException("Redis unavailable"));

		assertThat(adapter.findByMemberUuid(memberUuid)).isEmpty();
	}

	@Test
	void savesGradeManagementViewWithConfiguredTtl() throws Exception {
		GradeManagementView view = leafView();

		adapter.save(memberUuid, view);

		verify(valueOperations).set(
				eq(properties.memberKey(memberUuid)),
				eq(objectMapper.writeValueAsString(view)),
				eq(Duration.ofMinutes(10))
		);
	}

	@Test
	void doesNotThrowWhenRedisSaveFails() {
		doThrow(new RuntimeException("Redis unavailable"))
				.when(valueOperations)
				.set(eq(properties.memberKey(memberUuid)), anyString(), any(Duration.class));

		adapter.save(memberUuid, leafView());
	}

	@Test
	void deletesMemberCacheKeyOnEvict() {
		adapter.evict(memberUuid);

		verify(redisTemplate).delete(properties.memberKey(memberUuid));
	}

	@Test
	void doesNotThrowWhenRedisEvictFails() {
		when(redisTemplate.delete(properties.memberKey(memberUuid)))
				.thenThrow(new RuntimeException("Redis unavailable"));

		adapter.evict(memberUuid);
	}

	private GradeManagementView leafView() {
		return new GradeManagementView(
				new CurrentGradeView("LEAF", "🧳 잎새", 2, List.of()),
				new CurrentMetricsView(7L, 62L, 410L),
				new NextGradeView("TRAVELER", "✈️ 여행가", List.of()),
				new ProgressView(
						new MetricProgressView(7L, 10L, 3L, 70),
						new MetricProgressView(62L, 100L, 38L, 62),
						new MetricProgressView(410L, 500L, 90L, 82)
				),
				new CurrentBenefitSummaryView(
						"LEAF",
						"🧳 잎새",
						2,
						20,
						false,
						false,
						false,
						false,
						null
				)
		);
	}
}
