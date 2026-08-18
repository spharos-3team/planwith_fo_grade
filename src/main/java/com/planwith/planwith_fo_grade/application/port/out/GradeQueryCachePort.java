package com.planwith.planwith_fo_grade.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_grade.application.query.MemberGradeView;

public interface GradeQueryCachePort {

	Optional<MemberGradeView> findByMemberUuid(String memberUuid);

	void save(MemberGradeView view);

	void evict(String memberUuid);
}
