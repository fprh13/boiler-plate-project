package com.hello.boilerplate.domain.user.dto;

import com.hello.boilerplate.domain.user.entity.Role;
import com.hello.boilerplate.domain.user.entity.User;

public class UserRequestDto {
    public record Register(
            String loginId,
            String password,
            String email,
            String name
    ) {
        public User toEntity(final String encodedPassword) {
            return new User(loginId, encodedPassword, email, name, Role.USER);
        }
    }
}
