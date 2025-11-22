package com.hello.boilerplate.global.presentation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.hello.boilerplate.global.presentation.dto.ApiErrorResponse;

class ApiErrorResponseTest {
    @Test
    void 에러_응답을_생성합니다() {
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
    void 필드에러라면_400에러를_응답합니다() {
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