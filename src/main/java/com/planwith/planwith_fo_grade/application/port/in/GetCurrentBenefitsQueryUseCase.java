package com.planwith.planwith_fo_grade.application.port.in;

import com.planwith.planwith_fo_grade.application.query.CurrentBenefitSummaryView;

public interface GetCurrentBenefitsQueryUseCase {

	CurrentBenefitSummaryView get(String memberUuid);
}
