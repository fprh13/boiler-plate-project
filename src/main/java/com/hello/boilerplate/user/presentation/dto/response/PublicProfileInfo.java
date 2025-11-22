package com.hello.boilerplate.user.presentation.dto.response;

import com.hello.boilerplate.user.domain.User;

public record PublicProfileInfo(
	String email,
	String name
) {
	public static PublicProfileInfo from(User user) {
		return new PublicProfileInfo(user.getEmail(), user.getName());
	}
}
