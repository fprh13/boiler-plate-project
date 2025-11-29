package com.hello.boilerplate.auth.presentation.dto.request;

import jakarta.validation.constraints.Email;

public record FindLoginId(
	@Email String email
) {
}
