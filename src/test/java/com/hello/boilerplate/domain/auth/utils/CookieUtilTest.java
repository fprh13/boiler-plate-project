package com.hello.boilerplate.domain.auth.utils;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hello.boilerplate.domain.auth.exception.AuthorizationErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import com.hello.boilerplate.global.exception.CustomException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

class CookieUtilTest {

	private static final String COOKIE_NAME = "cookieName";
	private static final String COOKIE_VALUE = "cookieValue";
	private static final long COOKIE_EXPIRATION = 86400;

	@Test
	@DisplayName("쿠키를 정상적으로 발급한다")
	void shouldOfWhenNameValueAndMaxAgeGiven() {
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
	@DisplayName("무효화 쿠키를 정상적으로 발급한다")
	void shouldOfExpiredWhenNameGiven() {
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
	@DisplayName("쿠키의 이름이 null이면 예외를 던진다")
	void shouldThrowExceptionWhenNameIsNullOnOf() {
        //given
        String errorMessage = "서버에서 오류가 발생했습니다.";
		// then
		assertThatThrownBy(() -> CookieUtil.of(null, COOKIE_VALUE, COOKIE_EXPIRATION))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(errorMessage);
	}

	@Test
	@DisplayName("무효화 쿠키의 이름이 null이면 예외를 던진다")
	void shouldThrowExceptionWhenNameIsNullOnExpired() {
        //given
        String errorMessage = "서버에서 오류가 발생했습니다.";
		// then
		assertThatThrownBy(() -> CookieUtil.ofExpired(null))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(errorMessage);
	}

	@Test
	@DisplayName("쿠키의 이름이 비어있으면 예외를 던진다")
	void shouldThrowExceptionWhenNameIsBlankOnOf() {
        //given
        String errorMessage = "서버에서 오류가 발생했습니다.";
		// then
		assertThatThrownBy(() -> CookieUtil.of(null, COOKIE_VALUE, COOKIE_EXPIRATION))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(errorMessage);
	}

	@Test
	@DisplayName("무효화 쿠키의 이름이 비어있으면 예외를 던진다")
	void shouldThrowExceptionWhenNameIsBlankOnExpired() {
        //given
        String errorMessage = "서버에서 오류가 발생했습니다.";
		// then
		assertThatThrownBy(() -> CookieUtil.ofExpired(null))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(errorMessage);
	}

	@Test
	@DisplayName("찾는 이름의 쿠키가 요청에서 조회된다")
	void shouldFindCookieByNameWhenCookieExistsInRequest() {
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
	@DisplayName("찾는 이름의 쿠키가 요청에 없을 경우 예외를 던진다")
	void shouldThrowExceptionWhenCookieNameNotFoundInRequest() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie[] mockCookies = {new Cookie("differentCookie", COOKIE_VALUE)};
		when(request.getCookies()).thenReturn(mockCookies);

		assertThatThrownBy(() -> CookieUtil.findCookieByName(request, COOKIE_NAME))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(AuthorizationErrorMessages.INVALID_COOKIE_EXCEPTION);
	}

	@Test
	@DisplayName("요청된 쿠키가 없는 경우 예외를 던진다")
	void shouldThrowExceptionWhenRequestCookiesIsNull() {
		//given
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getCookies()).thenReturn(null);

		// when & then
		assertThatThrownBy(() -> CookieUtil.findCookieByName(request, COOKIE_NAME))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(AuthorizationErrorMessages.INVALID_COOKIE_EXCEPTION);
	}
}