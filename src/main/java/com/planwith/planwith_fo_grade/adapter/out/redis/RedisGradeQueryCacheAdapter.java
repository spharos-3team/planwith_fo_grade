package com.planwith.planwith_fo_grade.adapter.out.redis;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_grade.application.port.out.GradeQueryCachePort;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;
import com.planwith.planwith_fo_grade.config.GradeCacheProperties;

@Profile("!test")
@Component
public class RedisGradeQueryCacheAdapter implements GradeQueryCachePort {

	private static final Logger log = LoggerFactory.getLogger(RedisGradeQueryCacheAdapter.class);

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final GradeCacheProperties properties;

	public RedisGradeQueryCacheAdapter(
			StringRedisTemplate redisTemplate,
			ObjectMapper objectMapper,
			GradeCacheProperties properties
	) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	@Override
	public Optional<GradeManagementView> findByMemberUuid(String memberUuid) {
		try {
			String value = redisTemplate.opsForValue().get(properties.memberKey(memberUuid));
			if (value == null || value.isBlank()) {
				log.debug("RedisGradeQueryCacheAdapter : findByMemberUuid : 조회 캐시 MISS - memberUuid={}", memberUuid);
				return Optional.empty();
			}
			GradeManagementView view = objectMapper.readValue(value, GradeManagementView.class);
			log.debug("RedisGradeQueryCacheAdapter : findByMemberUuid : 조회 캐시 HIT - memberUuid={}", memberUuid);
			return Optional.of(view);
		} catch (Exception exception) {
			log.warn(
					"RedisGradeQueryCacheAdapter : findByMemberUuid : Redis 조회 실패로 MySQL 조회 가능 상태로 전환 - memberUuid={}",
					memberUuid,
					exception
			);
			return Optional.empty();
		}
	}

	@Override
	public void save(String memberUuid, GradeManagementView view) {
		try {
			Duration ttl = properties.getTtl() == null ? Duration.ofMinutes(10) : properties.getTtl();
			redisTemplate.opsForValue().set(
					properties.memberKey(memberUuid),
					objectMapper.writeValueAsString(view),
					ttl
			);
			log.debug("RedisGradeQueryCacheAdapter : save : 조회 캐시 저장 완료 - memberUuid={}", memberUuid);
		} catch (JsonProcessingException | RuntimeException exception) {
			log.warn("RedisGradeQueryCacheAdapter : save : Redis 저장 실패 - memberUuid={}", memberUuid, exception);
		}
	}

	@Override
	public void evict(String memberUuid) {
		try {
			redisTemplate.delete(properties.memberKey(memberUuid));
			log.debug("RedisGradeQueryCacheAdapter : evict : 조회 캐시 삭제 완료 - memberUuid={}", memberUuid);
		} catch (RuntimeException exception) {
			log.warn("RedisGradeQueryCacheAdapter : evict : Redis 삭제 실패 - memberUuid={}", memberUuid, exception);
		}
	}
}
