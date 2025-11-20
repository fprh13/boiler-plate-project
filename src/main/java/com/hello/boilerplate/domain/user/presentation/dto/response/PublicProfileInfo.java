package com.hello.boilerplate.domain.user.presentation.dto.response;

import com.hello.boilerplate.domain.user.domain.User;

public record PublicProfileInfo(
	String email,
	String name
) {
	public static PublicProfileInfo from(User user) {
		return new PublicProfileInfo(user.getEmail(), user.getName());
	}
}
