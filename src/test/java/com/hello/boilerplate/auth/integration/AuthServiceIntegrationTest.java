package com.hello.boilerplate.auth.integration;

import com.hello.boilerplate.auth.application.AuthService;
import com.hello.boilerplate.auth.application.RefreshTokenStore;
import com.hello.boilerplate.auth.presentation.dto.request.AuthenticateUser;
import com.hello.boilerplate.auth.presentation.dto.request.FindLoginId;
import com.hello.boilerplate.auth.presentation.dto.response.AuthenticationResult;
import com.hello.boilerplate.auth.presentation.dto.response.ReissuedToken;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.NotFoundException;
import com.hello.boilerplate.common.exception.UnauthorizedException;
import com.hello.boilerplate.common.infrastructure.mail.MailSender;
import com.hello.boilerplate.common.infrastructure.mail.TemplateRenderer;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.support.IntegrationSupportTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


class AuthServiceIntegrationTest extends IntegrationSupportTest {

	@Autowired AuthService authService;
	@Autowired UserRepository userRepository;
	@Autowired BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired RefreshTokenStore refreshTokenStore;
	@Autowired JwtUtil jwtUtil;
	@MockitoBean MailSender mailSender;
	@MockitoBean TemplateRenderer templateRenderer;

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

	@Nested
	@DisplayName("인증(로그인) 기능")
	class Authenticate {
		@Test
		void 인증을_한다() {
			//given
			User requestUser = UserFixture.USER_FIXTURE_1.create();

			AuthenticateUser authenticateUser = new AuthenticateUser(
				requestUser.getLoginId(),
				requestUser.getPassword()
			);

			//when
			AuthenticationResult authenticationResult = authService.authenticate(authenticateUser);

			//then
			assertAll(
				() -> assertThat(authenticationResult.accessToken()).isNotNull(),
				() -> assertThat(authenticationResult.refreshToken()).isNotNull()
			);
		}

		@Test
		void 아이디가_존재하지_않는다면_예외를_반환한다() {
		    //given
			User requestUser = UserFixture.USER_FIXTURE_1.create();

			AuthenticateUser authenticateUser = new AuthenticateUser(
				"nonExistentId",
				requestUser.getPassword()
			);

		    //when & then
			Assertions.assertThatThrownBy(() -> authService.authenticate(authenticateUser))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 비밀번호가_올바르지_않다면_예외를_반환한다() {
		    //given
			User requestUser = UserFixture.USER_FIXTURE_1.create();

			AuthenticateUser authenticateUser = new AuthenticateUser(
				requestUser.getLoginId(),
				"wrongPassword1234@"
			);

		    //when & then
			Assertions.assertThatThrownBy(() -> authService.authenticate(authenticateUser))
				.isInstanceOf(CustomException.class);
		}
	}

	@Nested
	@DisplayName("인증 무효화(로그아웃) 기능")
	class Invalidate {
		@Test
		void 인증_무효화를_한다() {
			//given
			String subject = user.getLoginId();
			String refreshToken = jwtUtil.createRefreshToken(user, new Date());
			refreshTokenStore.save(subject, refreshToken);

			//when
			authService.invalidate(subject);

			//then
			assertThat(refreshTokenStore.get(subject)).isNull();
		}
	}

	@Nested
	@DisplayName("토큰 재발급 기능")
	class ReissueToken {
		@Test
		void 토큰을_재발급_한다() {
			//given
			String refreshToken = jwtUtil.createRefreshToken(user, new Date());
			refreshTokenStore.save(user.getLoginId(), refreshToken);

			//when
			ReissuedToken result = authService.reissueToken(refreshToken);

			//then
			assertThat(result.accessToken()).isNotNull();
		}

		@Test
		void 저장된_재발급_토큰과_요청_재발급_토큰이_다르다면_예외를_반환한다() {
		    //given
			String requestRefreshToken = jwtUtil.createRefreshToken(user, new Date());
			refreshTokenStore.save(user.getLoginId(), requestRefreshToken);

			// Subject는 기존 유저와 동일하게, 다른 유저 정보를 통해 JWT 구성만 달리합니다.
			User otherUser = UserFixture.USER_FIXTURE_2.create();
			String storedRefreshToken = jwtUtil.createRefreshToken(otherUser, new Date());
			refreshTokenStore.save(user.getLoginId(), storedRefreshToken);

		    //when & then
			Assertions.assertThatThrownBy(() -> authService.reissueToken(requestRefreshToken))
				.isInstanceOf(UnauthorizedException.class);
		}

		@Test
		void 재발급_토큰의_Subject에_맞는_사용자가_없다면_예외를_반환한다() {
		    //given
			String refreshToken = jwtUtil.createRefreshToken(user, new Date());
			refreshTokenStore.save(user.getLoginId(), refreshToken);

			User otherUser = UserFixture.USER_FIXTURE_2.create();
			String otherUserRefreshToken = jwtUtil.createRefreshToken(otherUser, new Date());

		    //when & then
			Assertions.assertThatThrownBy(() -> authService.reissueToken(otherUserRefreshToken))
				.isInstanceOf(UnauthorizedException.class);
		}
	}

	@Nested
	@DisplayName("아이디 찾기 기능")
	class RetrieveLoginId {
		@Test
		void 아이디_찾기_이메일을_전송한다() {
		    //given
			String email = user.getEmail();
			FindLoginId findLoginId = new FindLoginId(email);

			//when
			authService.retrieveLoginId(findLoginId);

		    //then
			Mockito.verify(templateRenderer, Mockito.times(1))
				.render(Mockito.any(), Mockito.any());
			Mockito.verify(mailSender, Mockito.times(1))
				.send(Mockito.any(), Mockito.any(), Mockito.any());
		}

		@Test
		void 요청된_이메일에_맞는_사용자가_없다면_예외를_반환한다() {
		    //given
			String email = "wrong@wrong.com";
			FindLoginId findLoginId = new FindLoginId(email);

		    //when & then
		    Assertions.assertThatThrownBy(() -> authService.retrieveLoginId(findLoginId))
				.isInstanceOf(NotFoundException.class);
		}
	}
}