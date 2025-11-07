package com.hello.boilerplate.global.dto;

import org.springframework.validation.FieldError;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ResponseDto<T> {
    private static final String DEFAULT_SUCCESS_MESSAGE = "OK";
    private static final String FILED_ERROR_MESSAGE = "필드 값 유효하지 않음";

	private final String message;
	private final T data;

	@Builder(access = AccessLevel.PRIVATE)
	private ResponseDto(String message, T data) {
		this.message = message;
		this.data = data;
	}

    public static <T> ResponseDto<T> ofSuccess() {
        return ResponseDto.<T>builder()
                .message(DEFAULT_SUCCESS_MESSAGE)
                .data(null)
                .build();
    }

	public static <T> ResponseDto<T> ofSuccess(T data) {
		return ResponseDto.<T>builder()
			.message(DEFAULT_SUCCESS_MESSAGE)
			.data(data)
			.build();
	}

	public static <T> ResponseDto<T> ofSuccess(String message, T data) {
		return ResponseDto.<T>builder()
			.message(message)
			.data(data)
			.build();
	}

	public static ResponseDto<Void> ofFail(String message) {
		return ResponseDto.<Void>builder()
			.message(message)
			.data(null)
			.build();
	}

	public static ResponseDto<Void> ofFail(List<FieldError> fieldErrors) {
        FieldError fieldError = fieldErrors
                .get(fieldErrors.size() - 1);
		return ResponseDto.<Void>builder()
			.message(fieldError.getField() + FILED_ERROR_MESSAGE)
			.data(null)
			.build();
	}
}
