package com.hello.boilerplate.domain.user.presentation;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.presentation.dto.request.ChangePassword;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.domain.user.presentation.dto.request.UpdateUser;
import com.hello.boilerplate.domain.user.presentation.dto.response.ProfileInfo;
import com.hello.boilerplate.domain.user.presentation.dto.response.PublicProfileInfo;
import com.hello.boilerplate.global.exception.CustomException;
import com.hello.boilerplate.global.exception.NotFoundException;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.module.RestDocsSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends RestDocsSupport {

	private static final String BASE_URI = "/users";
	private static final String BASE_TAG = "User";
	private static final String BASE_SUCCESS_MESSAGE = "OK";
	private static final String BASE_FIELD_ERROR_MESSAGE = "의 필드 값 유효하지 않습니다.";

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
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isNotEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("회원 가입")
						.description("## 회원 가입 기능 \n"
							+ "### 사용법 \n"
							+ "- 필드의 validation을 확인해주세요.\n"
							+ "- 아이디와 이메일 중복 체크 완료 후 진행해주세요."
						)
						.requestSchema(Schema.schema("RegisterUser"))
							.requestFields(
								fieldWithPath("loginId").description("아이디는 영문 4자리 이상입니다.").type(JsonFieldType.STRING),
								fieldWithPath("password").description("비밀번호는 특수문자를 포함한 영문과 숫자 8자리 이상입니다.").type(JsonFieldType.STRING),
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
				post(BASE_URI)
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
				post(BASE_URI)
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
		void 회원가입_4XX_요청_데이터_유효성_검사_실패() throws Exception {
			//given
			String errorMessage = "password" + BASE_FIELD_ERROR_MESSAGE;

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser requestDto = new RegisterUser(
				userFixture.getLoginId(),
				"1234",
				userFixture.getEmail(),
				userFixture.getName()
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			Mockito.verify(userService, Mockito.never()).register(any());
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
	}

	@Nested
	@DisplayName("아이디 중복 체크 API 테스트")
	class CheckDuplicateLoginId {
		@Test
		void 아이디_중복_체크_2XX() throws Exception {
		    //given
			String loginId = "testLoginId";
			Mockito.doNothing().when(userService).checkDuplicateLoginId(loginId);

		    //when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/login-id/exists")
					.queryParam("loginId", loginId)
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("아이디 중복 체크")
						.description("## 아이디 중복 체크 기능 \n"
							+ "### 사용법 \n"
							+ "- 아이디를 쿼리 파라미터로 전송합니다.\n"
							+ "- 200응답이라면 사용 가능합니다."
						)
						.queryParameters(
							ResourceDocumentation.parameterWithName("loginId").description("검증 대상 아이디").type(SimpleType.STRING))
						.build())
					)
				);
		}

		@Test
		void 아이디_중복_체크_4XX_loingId_중복() throws Exception {
			//given
			String loginId = "testLoginId";

			String errorMessage = "이미 사용 중인 아이디입니다.";
			Mockito.doThrow(new CustomException(HttpStatus.CONFLICT, errorMessage))
				.when(userService)
				.checkDuplicateLoginId(loginId);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/login-id/exists")
					.queryParam("loginId", loginId)
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

	@Nested
	@DisplayName("이메일 중복 체크 API 테스트")
	class CheckDuplicateEmail {
		@Test
		void 이메일_중복_체크_2XX() throws Exception {
			//given
			String email = "test@test.com";
			Mockito.doNothing().when(userService).checkDuplicateEmail(email);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/email/exists")
					.queryParam("email", email)
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("이메일 중복 체크")
							.description("## 이메일 중복 체크 기능 \n"
								+ "### 사용법 \n"
								+ "- 이메일을 쿼리 파라미터로 전송합니다.\n"
								+ "- 200응답이라면 사용 가능합니다."
							)
							.queryParameters(
								ResourceDocumentation.parameterWithName("email").description("검증 대상 이메일").type(SimpleType.STRING))
							.build())
					)
				);
		}

		@Test
		void 이메일_중복_체크_4XX_email_중복() throws Exception {
			//given
			String email = "test@test.com";

			String errorMessage = "이미 사용 중인 이메일입니다.";
			Mockito.doThrow(new CustomException(HttpStatus.CONFLICT, errorMessage))
				.when(userService)
				.checkDuplicateEmail(email);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/email/exists")
					.queryParam("email", email)
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

	@Nested
	@DisplayName("프로필 조회 API 테스트")
	class GetProfileInfo {
		@Test
		void 프로필_조회_2XX() throws Exception {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			ProfileInfo profileInfo = ProfileInfo.from(userFixture);
			Mockito.when(userService.getProfileInfo(any(User.class))).thenReturn(profileInfo);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/profile")
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.loginId").value(profileInfo.loginId()))
				.andExpect(jsonPath("$.data.email").value(profileInfo.email()))
				.andExpect(jsonPath("$.data.name").value(profileInfo.name()))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("프로필 조회")
							.build())
					)
				);
		}
	}

	@Nested
	@DisplayName("공개 프로필 조회 API 테스트")
	class GetPublicProfileInfo {
		@Test
		void 공개_프로필_조회_2XX() throws Exception {
		    //given
			Long userId = 1L;
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			PublicProfileInfo publicProfileInfo = PublicProfileInfo.from(userFixture);
			Mockito.when(userService.getPublicProfileInfo(userId)).thenReturn(publicProfileInfo);

		    //when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/{userId}", userId)
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.email").value(publicProfileInfo.email()))
				.andExpect(jsonPath("$.data.name").value(publicProfileInfo.name()))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("공개 프로필 조회")
							.build())
					)
				);
		}

		@Test
		void 공개_프로필_조회_4XX_NOTFOUND() throws Exception {
		    //given
			String errorMessage = User.class.getSimpleName() + "을(를) 찾을 수 없습니다.";

			Long userId = 1L;
			Mockito.doThrow(new NotFoundException(User.class))
				.when(userService)
				.getPublicProfileInfo(userId);

		    //when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/{userId}", userId)
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isNotFound())
				.andExpect(
					result -> Assertions.assertInstanceOf(NotFoundException.class, result.getResolvedException())
				)
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
	@DisplayName("회원 정보 수정 API 테스트")
	class Update {
		@Test
		void 회원_정보_업데이트_2XX() throws Exception {
		    //given
			String changedName = "이름바꾸기";
			UpdateUser updateUser = new UpdateUser(changedName);

			Long userId = 1L;
			User user = UserFixture.USER_FIXTURE_1.create();
			ReflectionTestUtils.setField(user, "id", userId);

			Mockito.when(userService.update(any(UpdateUser.class), any(User.class))).thenReturn(userId);

		    //when
			ResultActions actions = mockMvc.perform(
				put(BASE_URI)
					.content(objectMapper.writeValueAsString(updateUser))
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").value(userId))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("회원 정보 수정")
							.description("## 회원 정보 수정 기능 \n"
								+ "### 사용법 \n"
								+ "- 필드의 validation을 확인해주세요.\n"
							)
							.requestSchema(Schema.schema("UpdateUser"))
							.requestFields(
								fieldWithPath("name").description("사용자 이름입니다.").type(JsonFieldType.STRING)
							)
							.build())
					)
				);
		}

		@Test
		void 회원_정보_업데이트_4XX_요청_데이터_유효성_검사_실패() throws Exception {
		    //given
			String errorMessage = "name" + BASE_FIELD_ERROR_MESSAGE;

			String changedName = "";
			UpdateUser updateUser = new UpdateUser(changedName);

		    //when
			ResultActions actions = mockMvc.perform(
				put(BASE_URI)
					.content(objectMapper.writeValueAsString(updateUser))
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			Mockito.verify(userService, Mockito.never()).update(any(), any());
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
	}

	@Nested
	@DisplayName("비밀번호 업데이트 기능 API 테스트")
	class UpdatePassword {
		@Test
		void 비밀번호_업데이트_2XX() throws Exception {
		    //given
		    String newPassword = "newPassword1234@";
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			ChangePassword changePassword = new ChangePassword(userFixture.getPassword(), newPassword);

			Mockito.doNothing().when(userService).updatePassword(any(ChangePassword.class), any(User.class));

			//when
			ResultActions actions = mockMvc.perform(
				patch(BASE_URI + "/password")
					.content(objectMapper.writeValueAsString(changePassword))
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("비밀번호 업데이트")
							.description("## 비밀번호 업데이트 기능 \n"
								+ "### 사용법 \n"
								+ "- 필드의 validation을 확인해주세요.\n"
							)
							.requestSchema(Schema.schema("ChangePassword"))
							.requestFields(
								fieldWithPath("password").description("기존 비밀번호입니다.").type(JsonFieldType.STRING),
								fieldWithPath("newPassword").description("새로운 비밀번호입니다.").type(JsonFieldType.STRING)
							)
							.build())
					)
				);
		}

		@Test
		void 비밀번호_업데이트_4XX_기존_비밀번호가_올바르지_않음() throws Exception {
		    //given
			String errorMessage = "비밀번호가 일치하지 않습니다.";

			String newPassword = "newPassword1234@";
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			ChangePassword changePassword = new ChangePassword(userFixture.getPassword(), newPassword);

			Mockito.doThrow(new CustomException(HttpStatus.BAD_REQUEST, errorMessage))
				.when(userService).updatePassword(any(ChangePassword.class), any(User.class));

			//when
			ResultActions actions = mockMvc.perform(
				patch(BASE_URI + "/password")
					.content(objectMapper.writeValueAsString(changePassword))
					.contentType(MediaType.APPLICATION_JSON));

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
		void 비밀번호_업데이트_4XX_요청_데이터_유효성_검사_실패() throws Exception {
		    //given
			String errorMessage = "newPassword" + BASE_FIELD_ERROR_MESSAGE;

			String newPassword = "1234";
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			ChangePassword changePassword = new ChangePassword(userFixture.getPassword(), newPassword);

		    //when
			ResultActions actions = mockMvc.perform(
				patch(BASE_URI + "/password")
					.content(objectMapper.writeValueAsString(changePassword))
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			Mockito.verify(userService, Mockito.never()).updatePassword(any(), any());
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
	}

	@Nested
	@DisplayName("회원 탈퇴 기능 API 테스트")
	class Withdraw {
		@Test
		void 회원_탈퇴_2XX() throws Exception {
		    //given
			Mockito.doNothing().when(userService).withdraw(any(User.class));

		    //when
			ResultActions actions = mockMvc.perform(
				delete(BASE_URI)
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("회원 탈퇴")
							.description("## 회원 탈퇴 기능 \n"
								+ "### 참고 \n"
								+ "- 서버의 권한 정보 및 클라이언트의 권한 쿠키를 초기화 합니다.\n"
							)
							.responseHeaders(
								headerWithName(HttpHeaders.SET_COOKIE).description("쿠키 초기화입니다.")
							)
							.build())
					)
				);
		}
	}
}