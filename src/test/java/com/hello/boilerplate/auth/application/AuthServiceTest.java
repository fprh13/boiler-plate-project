package com.hello.boilerplate.auth.application;

import com.hello.boilerplate.auth.presentation.dto.request.LoginRequestDto;
import com.hello.boilerplate.auth.presentation.dto.response.LoginResponseDto;
import com.hello.boilerplate.auth.presentation.dto.response.ReissueResponseDto;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.support.IntegrationSupportTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


class AuthServiceTest extends IntegrationSupportTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired RefreshTokenStore refreshTokenStore;
    @Autowired JwtUtil jwtUtil;

    private User user;

    @BeforeEach
    void setUp() {
        User userFixture = UserFixture.USER_FIXTURE_1.create();

        user = userRepository.save(
                new User(
                        userFixture.getLoginId(),
                        bCryptPasswordEncoder.encode(userFixture.getPassword()),
                        userFixture.getEmail(),
                        userFixture.getName(),
                        userFixture.getRole()
                )
        );
    }

    @Test
    void 로그인을_한다() {
        //given
        User requestUser = UserFixture.USER_FIXTURE_1.create();

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                requestUser.getLoginId(),
                requestUser.getPassword()
        );

        //when
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);

        //then
        assertAll(
            () -> assertThat(jwtUtil.getAccessTokenClaims(loginResponseDto.accessToken()).getSubject()).isEqualTo(user.getLoginId()),
            () -> assertThat(loginResponseDto.refreshToken()).isNotNull()
        );
    }

    @Test
    void 로그아웃을_한다() {
        //given
        String subject = user.getLoginId();

        //when
        authService.logout(subject);

		//then
		assertDoesNotThrow(() -> refreshTokenStore.get(subject));
    }

    @Test
    void 토큰을_재발급_한다() {
        //given
        String subject = user.getLoginId();
        String refreshToken = jwtUtil.createRefreshToken(user, new Date());
		refreshTokenStore.save(subject, refreshToken);

        //when
        ReissueResponseDto result = authService.reissue(subject, refreshToken);

        //then
        assertThat(jwtUtil.getAccessTokenClaims(result.accessToken()).getSubject()).isEqualTo(subject);
    }
}