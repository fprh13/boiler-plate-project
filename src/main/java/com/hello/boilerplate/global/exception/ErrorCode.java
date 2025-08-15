package com.hello.boilerplate.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	/**
	 * 400 BAD_REQUEST : 잘못된 요청
	 */
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청 데이터"),

	/**
	 * 401 UNAUTHORIZED : 인증 되지 않은 사용자
	 */
	AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증 필요"),
	INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "권한 정보가 없는 토큰"),
	EXPIRED_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰이 만료"),

	/**
	 * 403 FORBIDDEN : 권한 없음
	 */
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한 없음"),

	/**
	 * 404 NOT_FOUND : Resource 를 찾을 수 없음
	 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자 없음"),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리소스 없음"),

	/**
	 * 409 : CONFLICT : Resource 의 현재 상태와 충돌
	 */
	DUPLICATE_LOGINID(HttpStatus.CONFLICT, "아이디 중복"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이메일 중복"),
	DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "중복 데이터"),
	RESOURCE_CONFLICT(HttpStatus.CONFLICT, "리소스 상태 충돌"),
	OVER_VALIDATION(HttpStatus.CONFLICT, "저장 한도 초과"),

	/**
	 * 500 INTERNAL_SERVER_ERROR : 서버 오류
	 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류"),
	EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 호출 실패");

	private final HttpStatus httpStatus;
	private final String message;
}
