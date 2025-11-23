package com.hello.boilerplate.auth.integration;

import com.hello.boilerplate.auth.application.AuthService;
import com.hello.boilerplate.auth.application.RefreshTokenStore;
import com.hello.boilerplate.auth.presentation.dto.request.AuthenticateUser;
import com.hello.boilerplate.auth.presentation.dto.response.AuthenticationResult;
import com.hello.boilerplate.auth.presentation.dto.response.ReissuedToken;
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


class AuthServiceIntegrationTest extends IntegrationSupportTest {

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

        AuthenticateUser authenticateUser = new AuthenticateUser(
                requestUser.getLoginId(),
                requestUser.getPassword()
        );

        //when
        AuthenticationResult authenticationResult = authService.login(authenticateUser);

        //then
        assertAll(
            () -> assertThat(jwtUtil.getAccessTokenClaims(authenticationResult.accessToken()).getSubject()).isEqualTo(user.getLoginId()),
            () -> assertThat(authenticationResult.refreshToken()).isNotNull()
        );
    }

    @Test
    void 로그아웃을_한다() {
        //given
        String subject = user.getLoginId();
		refreshTokenStore.save(subject, jwtUtil.createRefreshToken(user, new Date()));

        //when
        authService.logout(subject);

		//then
		assertThat(refreshTokenStore.get(subject)).isNull();
    }

    @Test
    void 토큰을_재발급_한다() {
        //given
        String subject = user.getLoginId();
        String refreshToken = jwtUtil.createRefreshToken(user, new Date());
		refreshTokenStore.save(subject, refreshToken);

        //when
        ReissuedToken result = authService.reissue(subject, refreshToken);

        //then
        assertThat(jwtUtil.getAccessTokenClaims(result.accessToken()).getSubject()).isEqualTo(subject);
    }
}