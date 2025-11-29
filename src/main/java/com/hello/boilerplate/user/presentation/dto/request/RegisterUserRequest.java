package com.hello.boilerplate.user.presentation.dto.request;

import com.hello.boilerplate.user.domain.Role;
import com.hello.boilerplate.user.domain.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterUserRequest(
	@NotBlank
	@Pattern(regexp = "^[A-Za-z0-9]{4,20}$")
	String loginId,

	@NotBlank
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*?_~])[A-Za-z\\d!@#$%^&*?_~]{8,16}$")
	String password,

	@Email String email,
	@NotBlank String name
) {
	public User toEntity(final String encodedPassword) {
		return new User(loginId, encodedPassword, email, name, Role.USER);
	}
}
