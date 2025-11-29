package com.hello.boilerplate.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPassword(
	@NotBlank
	String token,
	@NotBlank
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*?_~])[A-Za-z\\d!@#$%^&*?_~]{8,16}$")
	String password
) {
}
