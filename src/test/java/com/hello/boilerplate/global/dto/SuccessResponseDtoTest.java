package com.hello.boilerplate.global.dto;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;


import java.util.List;

class SuccessResponseDtoTest {

	@Test
	@DisplayName("데이터가 주어졌을 때 성공 응답을 만듭니다")
	void shouldCreateSuccessResponseWhenDataGiven() {
		//given
		String data = "testData";

		//when
		SuccessResponseDto<String> successResponseDto = SuccessResponseDto.of(data);

		//then
		assertAll(
			() -> assertThat(successResponseDto.getMessage()).isEqualTo("OK"),
			() -> assertThat(successResponseDto.getData()).isEqualTo(data)
		);
	}

	@Test
	@DisplayName("데이터, 메세지가 주어졌을 때 메세지를 포함한 성공 응답을 만듭니다")
	void shouldCreateSuccessResponseWithMessageWhenDataAndMessageGiven() {
	    //given
		String message = "testMessage";
		String data = "testData";

	    //when
		SuccessResponseDto<String> successResponseDto = SuccessResponseDto.of(message, data);

		//then
	    assertAll(
			() -> assertThat(successResponseDto.getMessage()).isEqualTo(message),
	        () -> assertThat(successResponseDto.getData()).isEqualTo(data)
	    );
	}
}