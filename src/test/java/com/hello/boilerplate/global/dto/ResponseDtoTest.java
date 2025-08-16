package com.hello.boilerplate.global.dto;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.hello.boilerplate.global.exception.ErrorCode;

class ResponseDtoTest {

	@Test
	@DisplayName("상태, 데이터가 주어졌을 때 성공 응답을 만듭니다")
	void shouldCreateSuccessResponseWhenStatusAndDataGiven() {
		//given
		HttpStatus status = HttpStatus.OK;
		String data = "testData";

		//when
		ResponseDto<String> responseDto = ResponseDto.ofSuccess(status, data);

		//then
		assertAll(
			() -> assertThat(responseDto.getStatusCode()).isEqualTo(status.name()),
			() -> assertThat(responseDto.getMessage()).isNull(),
			() -> assertThat(responseDto.getErrorCode()).isNull(),
			() -> assertThat(responseDto.getData()).isEqualTo(data)
		);
	}

	@Test
	@DisplayName("상태, 데이터, 메세지가 주어졌을 때 메세지를 포함한 성공 응답을 만듭니다")
	void shouldCreateSuccessResponseWithMessageWhenStatusAndDataAndMessageGiven() {
	    //given
		HttpStatus status = HttpStatus.OK;
		String message = "testMessage";
		String data = "testData";

	    //when
		ResponseDto<String> responseDto = ResponseDto.ofSuccess(status, message, data);

		//then
	    assertAll(
	        () -> assertThat(responseDto.getStatusCode()).isEqualTo(status.name()),
			() -> assertThat(responseDto.getMessage()).isEqualTo(message),
			() -> assertThat(responseDto.getErrorCode()).isNull(),
	        () -> assertThat(responseDto.getData()).isEqualTo(data)
	    );
	}

	@Test
	@DisplayName("에러 코드가 주어졌을 때 에러 응답을 생성합니다")
	void shouldCreateErrorResponseWhenErrorCodeGiven() {
		//given
		ErrorCode internalServerError = ErrorCode.INTERNAL_SERVER_ERROR;

		//when
		ResponseDto<String> responseDto = ResponseDto.fromErrorCode(internalServerError);

		//then
		assertAll(
			() -> assertThat(responseDto.getStatusCode()).isEqualTo(internalServerError.getHttpStatus().name()),
			() -> assertThat(responseDto.getMessage()).isEqualTo(internalServerError.getMessage()),
			() -> assertThat(responseDto.getErrorCode()).isEqualTo(internalServerError.name()),
			() -> assertThat(responseDto.getData()).isNull()
		);
	}
}