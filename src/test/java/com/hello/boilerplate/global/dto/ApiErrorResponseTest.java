package com.hello.boilerplate.global.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ApiErrorResponseTest {
    @Test
    @DisplayName("에러 메세지가 주어졌을 때 에러 응답을 생성합니다")
    void shouldCreateErrorResponseWhenMessageGiven() {
        //given
        String message = "에러입니다.";

        //when
        ApiErrorResponse<Void> apiErrorResponse = ApiErrorResponse.of(message);

        //then
        assertAll(
                () -> assertThat(apiErrorResponse.getMessage()).isEqualTo(message),
                () -> assertThat(apiErrorResponse.getData()).isNull()
        );
    }

    @Test
    @DisplayName("필드에러 목록이 주어졌을 때 400 에러 응답을 생성합니다")
    void shouldCreate400FieldErrorResponseWhenFieldErrorsGiven() {
        //given
        String testObjectName = "fieldError";
        String testField = "testFieldError";
        String testDefaultMessage = "testMessage";
        List<FieldError> fieldErrors = List.of(new FieldError(testObjectName, testField, testDefaultMessage));

        String message = "의 필드 값 유효하지 않습니다.";

        //when
        ApiErrorResponse<Void> apiErrorResponse = ApiErrorResponse.of(fieldErrors);

        //then
        assertAll(
                () -> assertThat(apiErrorResponse.getMessage()).isEqualTo(testField + message),
                () -> assertThat(apiErrorResponse.getData()).isNull()
        );
    }

}