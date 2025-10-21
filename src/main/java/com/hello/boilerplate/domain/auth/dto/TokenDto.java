package com.hello.boilerplate.domain.auth.dto;

public record TokenDto (
        String accessToken,
        String refreshToken
) {
}
