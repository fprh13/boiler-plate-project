package com.hello.boilerplate.global.dto;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

	@Test
	@DisplayName("데이터가 주어졌을 때 성공 응답을 만듭니다")
	void shouldCreateSuccessResponseWhenDataGiven() {
		//given
		String data = "testData";

		//when
		ApiResponse<String> apiResponse = ApiResponse.of(data);

		//then
		assertAll(
			() -> assertThat(apiResponse.getMessage()).isEqualTo("OK"),
			() -> assertThat(apiResponse.getData()).isEqualTo(data)
		);
	}

	@Test
	@DisplayName("데이터, 메세지가 주어졌을 때 메세지를 포함한 성공 응답을 만듭니다")
	void shouldCreateSuccessResponseWithMessageWhenDataAndMessageGiven() {
	    //given
		String message = "testMessage";
		String data = "testData";

	    //when
		ApiResponse<String> apiResponse = ApiResponse.of(message, data);

		//then
	    assertAll(
			() -> assertThat(apiResponse.getMessage()).isEqualTo(message),
	        () -> assertThat(apiResponse.getData()).isEqualTo(data)
	    );
	}
}