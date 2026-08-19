package com.planwith.planwith_fo_grade.application.port.out;

import java.util.List;
import java.util.Optional;

import com.planwith.planwith_fo_grade.domain.model.MemberGradeMetric;
import com.planwith.planwith_fo_grade.domain.model.MemberMetricType;
import com.planwith.planwith_fo_grade.domain.model.vo.MemberUuid;

public interface MemberGradeMetricPort {

	Optional<MemberGradeMetric> findByMemberUuidAndMetricType(MemberUuid memberUuid, MemberMetricType metricType);

	List<MemberGradeMetric> findByMemberUuid(MemberUuid memberUuid);

	MemberGradeMetric save(MemberGradeMetric metric);
}
