package com.planwith.planwith_fo_grade.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_grade.domain.model.GradeRewardHistory;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

public interface GradeRewardHistoryPort {

	boolean existsByMemberUuidAndRewardMonth(MemberUuid memberUuid, String rewardMonth);

	Optional<GradeRewardHistory> findByMemberUuidAndRewardMonth(MemberUuid memberUuid, String rewardMonth);

	GradeRewardHistory save(GradeRewardHistory history);
}
