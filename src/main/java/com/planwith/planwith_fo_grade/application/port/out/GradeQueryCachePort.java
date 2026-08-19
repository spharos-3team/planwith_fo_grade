package com.planwith.planwith_fo_grade.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_grade.application.query.GradeManagementView;

public interface GradeQueryCachePort {

	Optional<GradeManagementView> findByMemberUuid(String memberUuid);

	void save(String memberUuid, GradeManagementView view);

	void evict(String memberUuid);
}
