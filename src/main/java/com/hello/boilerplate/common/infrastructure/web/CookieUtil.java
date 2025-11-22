package com.hello.boilerplate.common.infrastructure.web;

import com.hello.boilerplate.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;

import com.hello.boilerplate.common.exception.CustomException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieUtil {

	private static final String DEFAULT_PATH = "/";
	private static final String DEFAULT_SAME_SITE = "None";
	private static final String INVALID_COOKIE_EXCEPTION = "쿠키가 유효하지 않습니다.";


	public static ResponseCookie of(String name, String value, long cookieExpiration) {
		validateCookieName(name);
		return ResponseCookie.from(name, value)
			.maxAge(cookieExpiration)
			.path(DEFAULT_PATH)
			.sameSite(DEFAULT_SAME_SITE)
			.secure(true)
			.httpOnly(true)
			.build();
	}

	public static ResponseCookie ofExpired(String name) {
		validateCookieName(name);
		return ResponseCookie.from(name, "")
			.maxAge(0)
			.path(DEFAULT_PATH)
			.sameSite(DEFAULT_SAME_SITE)
			.secure(true)
			.httpOnly(true)
			.build();
	}

	public static Cookie findCookieByName(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
        throw new UnauthorizedException(INVALID_COOKIE_EXCEPTION);
	}

	private static void validateCookieName(String cookieName) {
		if (cookieName == null || cookieName.isEmpty()) {
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
