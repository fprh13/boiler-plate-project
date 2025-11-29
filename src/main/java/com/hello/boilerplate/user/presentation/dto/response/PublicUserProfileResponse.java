package com.hello.boilerplate.user.presentation.dto.response;

import com.hello.boilerplate.user.domain.User;

public record PublicUserProfileResponse(
	String email,
	String name
) {
	public static PublicUserProfileResponse from(User user) {
		return new PublicUserProfileResponse(user.getEmail(), user.getName());
	}
}
