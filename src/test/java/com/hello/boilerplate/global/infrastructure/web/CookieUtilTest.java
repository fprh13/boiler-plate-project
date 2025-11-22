package com.hello.boilerplate.global.infrastructure.web;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hello.boilerplate.domain.auth.exception.AuthorizationErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import com.hello.boilerplate.global.common.exception.CustomException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

class CookieUtilTest {

	private static final String COOKIE_NAME = "cookieName";
	private static final String COOKIE_VALUE = "cookieValue";
	private static final long COOKIE_EXPIRATION = 86400;

	@Test
	void 쿠키를_발급한다() {
		// when
		ResponseCookie cookie = CookieUtil.of(COOKIE_NAME, COOKIE_VALUE, COOKIE_EXPIRATION);

		// then
		assertAll(
			() -> assertThat(cookie).isNotNull(),
			() -> assertThat(cookie.getName()).isEqualTo(COOKIE_NAME),
			() -> assertThat(cookie.getValue()).isEqualTo(COOKIE_VALUE),
			() -> assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(COOKIE_EXPIRATION),
			() -> assertThat(cookie.getPath()).isEqualTo("/"),
			() -> assertThat(cookie.getSameSite()).isEqualTo("None"),
			() -> assertThat(cookie.isHttpOnly()).isTrue()
		);
	}

	@Test
	void 무효화_쿠키를_발급한다() {
		// when
		ResponseCookie cookie = CookieUtil.ofExpired(COOKIE_NAME);

		// then
		assertAll(
			() -> assertThat(cookie).isNotNull(),
			() -> assertThat(cookie.getName()).isEqualTo(COOKIE_NAME),
			() -> assertThat(cookie.getValue()).isEqualTo(""),
			() -> assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(0),
			() -> assertThat(cookie.getPath()).isEqualTo("/"),
			() -> assertThat(cookie.getSameSite()).isEqualTo("None"),
			() -> assertThat(cookie.isHttpOnly()).isTrue()
		);
	}

	@Test
	void 쿠키의_이름이_null이면_예외를_던진다() {
        //given
        String errorMessage = "서버에서 오류가 발생했습니다.";
		// then
		assertThatThrownBy(() -> CookieUtil.of(null, COOKIE_VALUE, COOKIE_EXPIRATION))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(errorMessage);
	}

	@Test
	@DisplayName("무효화 쿠키의 이름이 null이면 예외를 던진다")
	void 무효화_쿠키의_이름이_null이면_예외를_던진다() {
        //given
        String errorMessage = "서버에서 오류가 발생했습니다.";
		// then
		assertThatThrownBy(() -> CookieUtil.ofExpired(null))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(errorMessage);
	}

	@Test
	void 쿠키의_이름이_비어있으면_예외를_던진다() {
        //given
        String errorMessage = "서버에서 오류가 발생했습니다.";
		// then
		assertThatThrownBy(() -> CookieUtil.of(null, COOKIE_VALUE, COOKIE_EXPIRATION))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(errorMessage);
	}

	@Test
	void 무효화_쿠키의_이름이_비어있으면_예외를_던진다() {
        //given
        String errorMessage = "서버에서 오류가 발생했습니다.";
		// then
		assertThatThrownBy(() -> CookieUtil.ofExpired(null))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(errorMessage);
	}

	@Test
	void 찾는_이름의_쿠키가_요청에서_조회된다() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie[] mockCookies = {new Cookie(COOKIE_NAME, COOKIE_VALUE)};

		// when
		when(request.getCookies()).thenReturn(mockCookies);
		Cookie cookie = CookieUtil.findCookieByName(request, COOKIE_NAME);

		// then
		assertAll(
			() -> assertThat(cookie).isNotNull(),
			() -> assertThat(cookie.getName()).isEqualTo(COOKIE_NAME),
			() -> assertThat(cookie.getValue()).isEqualTo(COOKIE_VALUE)
		);
	}

	@Test
	void 찾는_이름의_쿠키가_요청에_없을_경우_예외를_던진다() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie[] mockCookies = {new Cookie("differentCookie", COOKIE_VALUE)};
		when(request.getCookies()).thenReturn(mockCookies);

		assertThatThrownBy(() -> CookieUtil.findCookieByName(request, COOKIE_NAME))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(AuthorizationErrorMessages.INVALID_COOKIE_EXCEPTION);
	}

	@Test
	void 요청된_쿠키가_없는_경우_예외를_던진다() {
		//given
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getCookies()).thenReturn(null);

		// when & then
		assertThatThrownBy(() -> CookieUtil.findCookieByName(request, COOKIE_NAME))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(AuthorizationErrorMessages.INVALID_COOKIE_EXCEPTION);
	}
}