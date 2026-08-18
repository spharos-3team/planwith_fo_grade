package com.planwith.planwith_fo_grade.domain.exception;

public class GradeNotFoundException extends GradeDomainException {

	private final String memberUuid;

	public GradeNotFoundException(String memberUuid) {
		super("GRADE_NOT_FOUND", "회원 등급 정보를 찾을 수 없습니다.");
		this.memberUuid = memberUuid;
	}

	public String memberUuid() {
		return memberUuid;
	}
}
