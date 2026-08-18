package com.planwith.planwith_fo_grade.domain.exception;

public class InvalidGradeException extends GradeDomainException {

	public InvalidGradeException(String message) {
		super("INVALID_GRADE", message);
	}
}
