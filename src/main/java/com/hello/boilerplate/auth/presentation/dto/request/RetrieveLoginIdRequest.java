package com.hello.boilerplate.auth.presentation.dto.request;

import jakarta.validation.constraints.Email;

public record RetrieveLoginIdRequest(
	@Email String email
) {
}
