package com.hello.boilerplate.auth.presentation.dto.request;

public record LoginRequestDto(
        String loginId,
        String password
) {
}
