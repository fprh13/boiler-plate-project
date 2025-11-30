package com.hello.boilerplate.auth.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RetrievePasswordRequest(
	@NotBlank
	@Pattern(regexp = "^[A-Za-z0-9]{4,20}$")
	String loginId,
	@Email
	String email
) {
}
