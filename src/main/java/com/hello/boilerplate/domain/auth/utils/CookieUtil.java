package com.hello.boilerplate.domain.auth.utils;

import org.springframework.http.ResponseCookie;

import com.hello.boilerplate.global.exception.CustomException;
import com.hello.boilerplate.global.exception.ErrorCode;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieUtil {

	private static final String DEFAULT_PATH = "/";
	private static final String DEFAULT_SAME_SITE = "None";


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
		throw new CustomException(ErrorCode.INVALID_REQUEST);
	}

	private static void validateCookieName(String cookieName) {
		if (cookieName == null || cookieName.isEmpty()) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}
