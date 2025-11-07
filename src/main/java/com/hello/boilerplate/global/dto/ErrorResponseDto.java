package com.hello.boilerplate.global.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

@Getter
public class ErrorResponseDto<T> {
    private static final String FILED_ERROR_MESSAGE = "필드 값 유효하지 않음";

    private final String message;
    private final T data;

    @Builder(access = AccessLevel.PRIVATE)
    private ErrorResponseDto(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static ErrorResponseDto<Void> of(String message) {
        return ErrorResponseDto.<Void>builder()
                .message(message)
                .data(null)
                .build();
    }

    public static ErrorResponseDto<Void> of(List<FieldError> fieldErrors) {
        FieldError fieldError = fieldErrors
                .get(fieldErrors.size() - 1);
        return ErrorResponseDto.<Void>builder()
                .message(fieldError.getField() + FILED_ERROR_MESSAGE)
                .data(null)
                .build();
    }
}
