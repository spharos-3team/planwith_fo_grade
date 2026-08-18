package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.query.GetMemberGradeQuery;
import com.planwith.planwith_fo_grade.application.query.MemberGradeView;

public interface GetMemberGradeQueryUseCase {

	MemberGradeView getCurrentGrade(GetMemberGradeQuery query);
}
