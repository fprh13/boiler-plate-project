package com.hello.boilerplate.domain.user.presentation;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.global.exception.CustomException;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.module.RestDocsSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends RestDocsSupport {

	private static final String Base_URI = "/users";
	private static final String BASE_TAG = "User";

	@Nested
	@DisplayName("회원가입 API 테스트")
	class RegisterTest {
		@Test
		void 회원가입_2XX() throws Exception {
			//given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			Mockito.when(userService.register(any(RegisterUser.class)))
				.thenReturn(any(Long.class));
			RegisterUser requestDto = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(Base_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("OK"))
				.andExpect(jsonPath("$.data").isNotEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("회원 가입")
						.description("## 회원 가입 기능 \n"
							+ "### 사용법 \n"
							+ "- 필드의 validation을 확인해주세요.\n"
							+ "- 아이디와 이메일 중복 체크 완료 후 진행해주세요.")
						.requestSchema(Schema.schema("RegisterUser"))
							.requestFields(
								fieldWithPath("loginId").description("아이디는 영문 4자리 이상입니다.").type(JsonFieldType.STRING),
								fieldWithPath("password").description("비밀번호는 8자리 이상입니다.").type(JsonFieldType.STRING),
								fieldWithPath("email").description("이메일 형식을 지켜주세요.").type(JsonFieldType.STRING),
								fieldWithPath("name").description("사용자 이름입니다.").type(JsonFieldType.STRING)
							)
						.build())
				));
		}

		@Test
		void 회원가입_4XX_아이디_중복() throws Exception {
			//given
			String errorMessage = "이미 사용 중인 아이디입니다.";

			Mockito.doThrow(new CustomException(HttpStatus.CONFLICT, errorMessage))
				.when(userService)
				.register(any(RegisterUser.class));

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser requestDto = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(Base_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isConflict())
				.andExpect(result -> Assertions.assertInstanceOf(CustomException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.build())
					)
				);
		}

		@Test
		void 회원가입_4XX_이메일_중복() throws Exception {
			//given
			String errorMessage = "이미 사용 중인 이메일입니다.";

			Mockito.doThrow(new CustomException(HttpStatus.CONFLICT, errorMessage))
				.when(userService)
				.register(any(RegisterUser.class));

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser requestDto = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(Base_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isConflict())
				.andExpect(result -> Assertions.assertInstanceOf(CustomException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.build())
					)
				);
		}
	}
}