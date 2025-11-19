package com.hello.boilerplate.domain.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePassword(
	@NotBlank
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*?_~])[A-Za-z\\d!@#$%^&*?_~]{8,16}$")
	String password,

	@NotBlank
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*?_~])[A-Za-z\\d!@#$%^&*?_~]{8,16}$")
	String newPassword
) {
}
