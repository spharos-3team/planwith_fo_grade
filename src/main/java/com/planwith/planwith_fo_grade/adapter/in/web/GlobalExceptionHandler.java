package com.planwith.planwith_fo_grade.adapter.in.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.planwith.planwith_fo_grade.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_grade.domain.exception.GradeDomainException;
import com.planwith.planwith_fo_grade.domain.exception.GradeNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
		);
		log.warn("GlobalExceptionHandler : handleValidation : 요청값 검증 실패 - fieldCount={}",
				fieldErrors.size());
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "요청값이 올바르지 않습니다.", fieldErrors)
		);
	}

	@ExceptionHandler(GradeNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleGradeNotFound(GradeNotFoundException exception) {
		log.warn("GlobalExceptionHandler : handleGradeNotFound : 회원 등급 조회 실패 - memberUuid={}",
				exception.memberUuid());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
				ApiResponse.failure(exception.errorCode(), exception.getMessage(), Map.of())
		);
	}

	@ExceptionHandler(GradeDomainException.class)
	public ResponseEntity<ApiResponse<Void>> handleGradeDomain(GradeDomainException exception) {
		log.warn("GlobalExceptionHandler : handleGradeDomain : 등급 도메인 규칙 위반 - errorCode={}",
				exception.errorCode());
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(
				ApiResponse.failure(exception.errorCode(), exception.getMessage(), Map.of())
		);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		log.warn("GlobalExceptionHandler : handleTypeMismatch : 요청 파라미터 타입 불일치 - parameter={}",
				exception.getName());
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "요청값이 올바르지 않습니다.", Map.of())
		);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		log.warn("GlobalExceptionHandler : handleUnreadableRequest : 요청 본문 파싱 실패");
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "요청 본문을 읽을 수 없습니다.", Map.of())
		);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingRequestParameter(
			MissingServletRequestParameterException exception
	) {
		log.warn("GlobalExceptionHandler : handleMissingRequestParameter : 필수 요청 파라미터 누락 - parameter={}",
				exception.getParameterName());
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "필수 요청 파라미터가 없습니다.", Map.of())
		);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeader(MissingRequestHeaderException exception) {
		if ("X-Member-UUID".equals(exception.getHeaderName())) {
			log.warn("GlobalExceptionHandler : handleMissingRequestHeader : 인증 회원 식별자 헤더 누락");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
					ApiResponse.failure("AUTHENTICATION_REQUIRED", "인증이 필요합니다.", Map.of())
			);
		}
		log.warn("GlobalExceptionHandler : handleMissingRequestHeader : 필수 요청 헤더 누락 - header={}",
				exception.getHeaderName());
		return ResponseEntity.badRequest().body(
				ApiResponse.failure("INVALID_REQUEST", "필수 요청 헤더가 없습니다.", Map.of())
		);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException exception) {
		log.warn("GlobalExceptionHandler : handleNoResource : 요청 경로 없음 - path={}",
				exception.getResourcePath());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
				ApiResponse.failure("NOT_FOUND", "요청한 경로를 찾을 수 없습니다.", Map.of())
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
		log.error("GlobalExceptionHandler : handleUnexpectedException : 예상하지 못한 시스템 오류 발생", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				ApiResponse.failure("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", Map.of())
		);
	}
}
