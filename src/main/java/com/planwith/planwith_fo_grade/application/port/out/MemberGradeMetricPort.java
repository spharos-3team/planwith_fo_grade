package com.planwith.planwith_fo_grade.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

public interface MemberGradeMetricPort {

	Optional<MemberGradeMetric> findByMemberUuidAndMetricType(MemberUuid memberUuid, MemberMetricType metricType);

	MemberGradeMetric save(MemberGradeMetric metric);
}
