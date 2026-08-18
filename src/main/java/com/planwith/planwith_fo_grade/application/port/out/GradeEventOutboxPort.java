package com.planwith.planwith_fo_grade.application.port.out;

public interface GradeEventOutboxPort {

	void save(GradeOutboxMessage message);
}
