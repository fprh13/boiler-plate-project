package com.hello.boilerplate.user.presentation.dto.response;

import com.hello.boilerplate.user.domain.User;

public record UserProfileResponse(
	String loginId,
	String email,
	String name
) {
	public static UserProfileResponse from(User user) {
		return new UserProfileResponse(user.getLoginId(), user.getEmail(), user.getName());
	}
}
