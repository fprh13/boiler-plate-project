package com.hello.boilerplate.domain.user.application;

import com.hello.boilerplate.domain.user.dto.UserRequestDto;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.module.IntegrationSupportTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest extends IntegrationSupportTest {

    @Autowired UserService userService;

    @Test
    public void shouldRegisterWhenUserRequestDtoGiven() {
        //given
        User userFixture = UserFixture.USER_FIXTURE_1.create();
        UserRequestDto.Register requestDto = new UserRequestDto.Register(
                userFixture.getLoginId(),
                userFixture.getPassword(),
                userFixture.getEmail(),
                userFixture.getName()
        );
        //when
        Long result = userService.register(requestDto);

        //then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Long.class);
    }
}