package com.hello.boilerplate.domain.auth.dto;

public record LoginResponseDto (
        String accessToken,
        String refreshToken
) {
}
