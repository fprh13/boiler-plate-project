package com.hello.boilerplate.auth.presentation.dto.response;

public record AuthenticationResult(
        String accessToken,
        String refreshToken
) {
}
