package com.hello.boilerplate.auth.presentation.dto.request;

public record LoginUser(
        String loginId,
        String password
) {
}
