package com.planwith.planwith_fo_grade.adapter.out.redis;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_grade.application.port.out.GradeQueryCachePort;
import com.planwith.planwith_fo_grade.application.query.GradeManagementView;

@Profile("test")
@Component
public class InMemoryGradeQueryCacheAdapter implements GradeQueryCachePort {

	private final Map<String, GradeManagementView> values = new ConcurrentHashMap<>();

	@Override
	public Optional<GradeManagementView> findByMemberUuid(String memberUuid) {
		return Optional.ofNullable(values.get(memberUuid));
	}

	@Override
	public void save(String memberUuid, GradeManagementView view) {
		values.put(memberUuid, view);
	}

	@Override
	public void evict(String memberUuid) {
		values.remove(memberUuid);
	}

	public boolean contains(String memberUuid) {
		return values.containsKey(memberUuid);
	}

	public int size() {
		return values.size();
	}
}
