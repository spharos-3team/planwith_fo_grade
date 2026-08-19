package com.planwith.planwith_fo_grade.application.port.in;

import java.util.List;

import com.planwith.planwith_fo_grade.application.query.GradeCatalogView;

public interface GetAllGradesQueryUseCase {

	List<GradeCatalogView> listAll();
}
