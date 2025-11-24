package com.hello.boilerplate.auth.presentation;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.hello.boilerplate.auth.presentation.dto.request.AuthenticateUser;
import com.hello.boilerplate.auth.presentation.dto.response.AuthenticationResult;
import com.hello.boilerplate.auth.presentation.dto.response.ReissuedToken;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.UnauthorizedException;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.support.RestDocsSupport;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends RestDocsSupport {

	private static final String BASE_URI = "/auth";
	private static final String BASE_TAG = "Auth";
	private static final String BASE_SUCCESS_MESSAGE = "OK";
	private static final String BASE_FIELD_ERROR_MESSAGE = "의 필드 값 유효하지 않습니다.";

    private static final String TEST_ACCESS_TOKEN = "accessabcdefghijklmnopqrstuvwxyz";
    private static final String TEST_REFRESH_TOKEN = "refreshabcdefghijklmnopqrstuvwxyz";

	@Nested
	@DisplayName("인증(로그인) API 테스트")
	class Authenticate {
		@Test
		void 로그인_2XX() throws Exception {
			//given
			User userFixture = UserFixture.USER_FIXTURE_1.create();

			AuthenticateUser requestDto = new AuthenticateUser(userFixture.getLoginId(), userFixture.getPassword());
			AuthenticationResult responseDto = new AuthenticationResult(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);

			Mockito.when(authService.authenticate(requestDto))
				.thenReturn(responseDto);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/login")
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON)
			);

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("로그인")
							.description("## 로그인 기능\n"
								+ "### 사용법 \n"
								+ "- 필드의 validation을 확인해주세요.\n"
								+ "- accessToken은 Authorization 헤더로 전송됩니다.\n"
								+ "- refreshToken은 쿠키로 전송되어 브라우저에 삽입됩니다.\n"
								+ "### 필독 \n"
								+ "- accessToken이란? 권한이 필요한 API에 함께 보내야되는 인증 토큰입니다.\n"
								+ "- refreshToken이란? accessToken이 만료되어 새로 발급받아야할 때 사용되는 토큰입니다.\n"
							)
							.requestSchema(Schema.schema("AuthenticateUser"))
							.requestFields(
								fieldWithPath("loginId").description("아이디는 영문 4자리 이상입니다.").type(JsonFieldType.STRING),
								fieldWithPath("password").description("비밀번호는 특수문자를 포함한 영문과 숫자 8자리 이상입니다.").type(JsonFieldType.STRING)
							)
							.build()
						)
					)
				);
		}

		@Test
		void 로그인_4XX_요청_데이터_유효성_검사_실패() throws Exception {
		    //given
			String errorMessage = "password" + BASE_FIELD_ERROR_MESSAGE;

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser requestDto = new AuthenticateUser(userFixture.getLoginId(), "1234");

		    //when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/login")
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON)
			);

		    //then
			actions
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.build())
					)
				);
		}

		@Test
		void 로그인_4XX_아이디_존재하지_않음() throws Exception {
		    //given
			String errorMessage = "아이디 혹은 비밀번호가 일치하지 않습니다.";

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser requestDto = new AuthenticateUser(userFixture.getLoginId(), userFixture.getPassword());

			Mockito.doThrow(new CustomException(HttpStatus.BAD_REQUEST, errorMessage))
				.when(authService).authenticate(requestDto);

		    //when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/login")
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON)
			);

		    //then
			actions
				.andExpect(status().isBadRequest())
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
		void 로그인_4XX_비밀번호가_올바르지_않음() throws Exception {
		    //given
			String errorMessage = "아이디 혹은 비밀번호가 일치하지 않습니다.";

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser requestDto = new AuthenticateUser(userFixture.getLoginId(), "wrong1234@");

			Mockito.doThrow(new CustomException(HttpStatus.BAD_REQUEST, errorMessage))
				.when(authService).authenticate(requestDto);

		    //when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/login")
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON)
			);

		    //then
			actions
				.andExpect(status().isBadRequest())
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

	@Nested
	@DisplayName("인증 무효화(로그아웃) 기능")
	class Invalidate {
		@Test
		void 로그아웃_2XX() throws Exception {
			//given
			String subject = "testLoginId";
			Mockito.doNothing().when(authService).invalidate(subject);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/logout")
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("로그아웃")
							.description("- 로그아웃 입니다. 브라우저 쿠키를 초기화 합니다.")
							.build()
						)
					)
				);
		}
	}

	@Nested
	@DisplayName("토큰 재발급 기능")
	class ReissueToken {
		@Test
		void 재발급_2XX() throws Exception {
			//given
			Cookie requestCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, TEST_REFRESH_TOKEN);

			ReissuedToken responseDto = new ReissuedToken("newAccessToken");
			Mockito.when(authService.reissueToken(TEST_REFRESH_TOKEN))
				.thenReturn(responseDto);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/reissue")
					.cookie(requestCookie)
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("재발급")
							.description("## 재발급 기능 \n"
								+ "### 사용법 \n"
								+ "- 재발급 시 새로운 엑세스 토큰을 헤더에 응답합니다.\n"
								+ "- 401이 뜬다면, 재로그인이 필요합니다.\n"
							)
							.build()
						)
					)
				);
		}

		@Test
		void 토큰_재발급_4XX_재발급_토큰_쿠키가_전송되지_않음() throws Exception {
		    //given
			String errorMessage = "쿠키가 유효하지 않습니다.";

		    //when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/reissue")
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isUnauthorized())
				.andExpect(result -> Assertions.assertInstanceOf(UnauthorizedException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.build())
					)
				);
		}

		@Test
		void 토큰_재발급_4XX_재발급_토큰이_서버에_존재하지_않은_경우() throws Exception {
		    //given
			String errorMessage = "토큰이 유효하지 않습니다.";

			Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, TEST_REFRESH_TOKEN);

			Mockito.when(authService.reissueToken(TEST_REFRESH_TOKEN))
				.thenThrow(new UnauthorizedException(errorMessage));

		    //when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/reissue")
					.cookie(refreshTokenCookie)
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isUnauthorized())
				.andExpect(result -> Assertions.assertInstanceOf(UnauthorizedException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.build())
					)
				);
		}

		@Test
		void 토큰_재발급_4XX_서버의_정보와_요청_재발급_토큰이_다른_경우() throws Exception {
			//given
			String errorMessage = "토큰이 유효하지 않습니다.";

			Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, TEST_REFRESH_TOKEN);

			Mockito.when(authService.reissueToken(TEST_REFRESH_TOKEN))
				.thenThrow(new UnauthorizedException(errorMessage));

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/reissue")
					.cookie(refreshTokenCookie)
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isUnauthorized())
				.andExpect(result -> Assertions.assertInstanceOf(UnauthorizedException.class, result.getResolvedException()))
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