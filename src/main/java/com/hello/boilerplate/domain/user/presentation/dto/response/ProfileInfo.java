package com.hello.boilerplate.domain.user.presentation.dto.response;

import com.hello.boilerplate.domain.user.domain.User;

public record ProfileInfo(
	String loginId,
	String email,
	String name
) {
	public static ProfileInfo from(User user) {
		return new ProfileInfo(user.getLoginId(), user.getEmail(), user.getName());
	}
}
