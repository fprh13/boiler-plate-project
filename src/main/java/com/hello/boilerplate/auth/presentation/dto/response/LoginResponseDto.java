package com.hello.boilerplate.auth.presentation.dto.response;

public record LoginResponseDto (
        String accessToken,
        String refreshToken
) {
}
