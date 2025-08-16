package com.hello.boilerplate.global.dto;

import org.springframework.http.HttpStatus;

import com.hello.boilerplate.global.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ResponseDto<T> {

	private final String statusCode;
	private final String message;
	private final String errorCode;
	private final T data;

	@Builder(access = AccessLevel.PRIVATE)
	private ResponseDto(String statusCode, String message, String errorCode, T data) {
		this.statusCode = statusCode;
		this.message = message;
		this.errorCode = errorCode;
		this.data = data;
	}

	public static <T> ResponseDto<T> ofSuccess(HttpStatus status, T data) {
		return ResponseDto.<T>builder()
			.statusCode(status.name())
			.message(null)
			.errorCode(null)
			.data(data)
			.build();
	}

	public static <T> ResponseDto<T> ofSuccess(HttpStatus status, String message, T data) {
		return ResponseDto.<T>builder()
			.statusCode(status.name())
			.message(message)
			.errorCode(null)
			.data(data)
			.build();
	}

	public static <T> ResponseDto<T> fromErrorCode(ErrorCode errorCode) {
		return ResponseDto.<T>builder()
			.statusCode(errorCode.getHttpStatus().name())
			.message(errorCode.getMessage())
			.errorCode(errorCode.name())
			.data(null)
			.build();
	}
}
