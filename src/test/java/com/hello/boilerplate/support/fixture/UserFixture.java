package com.hello.boilerplate.support.fixture;

import com.hello.boilerplate.domain.user.entity.Role;
import com.hello.boilerplate.domain.user.entity.User;

public enum UserFixture {
    USER_FIXTURE_1("test1", "test1@1234", "test1@gmail.com", "홍길동"),
    USER_FIXTURE_2("test2", "test2@1234", "test2@gmail.com", "존도"),
    USER_FIXTURE_3("test3", "test3@1234", "test3#gmail.com", "제인도");

    private final String loginId;
    private final String password;
    private final String email;
    private final String name;

    UserFixture(String loginId, String password, String email, String name) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.name = name;
    }

    public User create() {
        return new User(loginId, password, email, name, Role.USER);
    }
}
