package com.hello.boilerplate.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPasswordCode(
	@NotBlank
	@Pattern(regexp = "^[A-Za-z0-9]{4,20}$")
	String loginId,
	@NotBlank
	String code
) {
}
