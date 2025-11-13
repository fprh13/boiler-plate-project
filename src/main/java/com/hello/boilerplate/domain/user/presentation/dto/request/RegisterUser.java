package com.hello.boilerplate.domain.user.presentation.dto.request;

import com.hello.boilerplate.domain.user.domain.Role;
import com.hello.boilerplate.domain.user.domain.User;

public record RegisterUser(
	String loginId,
	String password,
	String email,
	String name
) {
	public User toEntity(final String encodedPassword) {
		return new User(loginId, encodedPassword, email, name, Role.USER);
	}
}
