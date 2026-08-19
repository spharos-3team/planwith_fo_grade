package com.planwith.planwith_fo_grade.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_grade.domain.model.GradeMember;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

public interface GradeMemberPort {

	Optional<GradeMember> findByMemberUuid(MemberUuid memberUuid);

	GradeMember save(GradeMember member);
}
