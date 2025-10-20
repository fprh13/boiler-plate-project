package com.hello.boilerplate.domain.auth.dto;

public record LoginRequestDto(
        String loginId,
        String password
) {
}
