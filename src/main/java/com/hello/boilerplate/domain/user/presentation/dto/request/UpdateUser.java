package com.hello.boilerplate.domain.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUser(
	@NotBlank String name
) {
}
