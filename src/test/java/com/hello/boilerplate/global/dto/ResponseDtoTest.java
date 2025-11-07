package com.hello.boilerplate.global.dto;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;


import java.util.List;

class ResponseDtoTest {

	@Test
	@DisplayName("데이터가 주어졌을 때 성공 응답을 만듭니다")
	void shouldCreateSuccessResponseWhenDataGiven() {
		//given
		String data = "testData";

		//when
		ResponseDto<String> responseDto = ResponseDto.ofSuccess(data);

		//then
		assertAll(
			() -> assertThat(responseDto.getMessage()).isEqualTo("OK"),
			() -> assertThat(responseDto.getData()).isEqualTo(data)
		);
	}

	@Test
	@DisplayName("데이터, 메세지가 주어졌을 때 메세지를 포함한 성공 응답을 만듭니다")
	void shouldCreateSuccessResponseWithMessageWhenDataAndMessageGiven() {
	    //given
		String message = "testMessage";
		String data = "testData";

	    //when
		ResponseDto<String> responseDto = ResponseDto.ofSuccess(message, data);

		//then
	    assertAll(
			() -> assertThat(responseDto.getMessage()).isEqualTo(message),
	        () -> assertThat(responseDto.getData()).isEqualTo(data)
	    );
	}

	@Test
	@DisplayName("에러 메세지가 주어졌을 때 에러 응답을 생성합니다")
	void shouldCreateErrorResponseWhenMessageGiven() {
		//given
        String message = "에러입니다.";

		//when
		ResponseDto<Void> responseDto = ResponseDto.ofFail(message);

		//then
		assertAll(
			() -> assertThat(responseDto.getMessage()).isEqualTo(message),
			() -> assertThat(responseDto.getData()).isNull()
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

        String message = "필드 값 유효하지 않음";

		//when
		ResponseDto<Void> responseDto = ResponseDto.ofFail(fieldErrors);

		//then
		assertAll(
			() -> assertThat(responseDto.getMessage()).isEqualTo(testField + message),
			() -> assertThat(responseDto.getData()).isNull()
		);
	}
}