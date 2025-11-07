package com.hello.boilerplate.global.dto;

import org.springframework.validation.FieldError;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class SuccessResponseDto<T> {
    private static final String DEFAULT_SUCCESS_MESSAGE = "OK";

	private final String message;
	private final T data;

	@Builder(access = AccessLevel.PRIVATE)
	private SuccessResponseDto(String message, T data) {
		this.message = message;
		this.data = data;
	}

    public static <T> SuccessResponseDto<T> of() {
        return SuccessResponseDto.<T>builder()
                .message(DEFAULT_SUCCESS_MESSAGE)
                .data(null)
                .build();
    }

	public static <T> SuccessResponseDto<T> of(T data) {
		return SuccessResponseDto.<T>builder()
			.message(DEFAULT_SUCCESS_MESSAGE)
			.data(data)
			.build();
	}

	public static <T> SuccessResponseDto<T> of(String message, T data) {
		return SuccessResponseDto.<T>builder()
			.message(message)
			.data(data)
			.build();
	}
}
