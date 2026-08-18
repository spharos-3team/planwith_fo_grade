package com.planwith.planwith_fo_grade.domain.exception;

public class GradeDomainException extends RuntimeException {

	private final String errorCode;

	public GradeDomainException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String errorCode() {
		return errorCode;
	}
}
