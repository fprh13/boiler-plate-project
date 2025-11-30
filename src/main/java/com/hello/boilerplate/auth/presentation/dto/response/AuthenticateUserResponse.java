package com.hello.boilerplate.auth.presentation.dto.response;

public record AuthenticateUserResponse(
        String accessToken,
        String refreshToken
) {
}
