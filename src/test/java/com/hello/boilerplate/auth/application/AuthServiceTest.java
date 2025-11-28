package com.hello.boilerplate.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.auth.presentation.dto.request.AuthenticateUser;
import com.hello.boilerplate.auth.presentation.dto.request.FindLoginId;
import com.hello.boilerplate.auth.presentation.dto.request.FindPassword;
import com.hello.boilerplate.auth.presentation.dto.response.AuthenticationResult;
import com.hello.boilerplate.auth.presentation.dto.response.ReissuedToken;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.NotFoundException;
import com.hello.boilerplate.common.exception.UnauthorizedException;
import com.hello.boilerplate.common.infrastructure.mail.MailSender;
import com.hello.boilerplate.common.infrastructure.mail.TemplateRenderer;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks AuthService authService;
	@Mock UserRepository userRepository;
	@Mock BCryptPasswordEncoder bCryptPasswordEncoder;
	@Mock RefreshTokenStore refreshTokenStore;
	@Mock JwtUtil jwtUtil;
	@Mock TemplateRenderer templateRenderer;
	@Mock MailSender mailSender;
	@Mock VerificationCodeStore verificationCodeStore;

	@Nested
	@DisplayName("인증(로그인) 기능")
	class Authenticate {
		@Test
		void 아이디로_사용자를_조회한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser authenticateUser = new AuthenticateUser(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findUserByLoginId(authenticateUser.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUser.password(), user.getPassword()))
				.thenReturn(true);

			//when
			authService.authenticate(authenticateUser);

			//then
			Mockito.verify(userRepository, Mockito.times(1))
				.findUserByLoginId(authenticateUser.loginId());
		}

		@Test
		void 아이디에_해당하는_사용자가_없다면_예외를_반환한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser authenticateUser = new AuthenticateUser(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findUserByLoginId(authenticateUser.loginId()))
				.thenThrow(CustomException.class);

		    //when & then
			assertThatThrownBy(() -> authService.authenticate(authenticateUser))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 비밀번호를_검증한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser authenticateUser = new AuthenticateUser(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findUserByLoginId(authenticateUser.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUser.password(), user.getPassword()))
				.thenReturn(true);

		    //when
			authService.authenticate(authenticateUser);

		    //then
		    Mockito.verify(bCryptPasswordEncoder, Mockito.times(1))
				.matches(authenticateUser.password(), user.getPassword());
		}

		@Test
		void 비밀번호_검증에_실패한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser authenticateUser = new AuthenticateUser(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findUserByLoginId(authenticateUser.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUser.password(), user.getPassword()))
				.thenReturn(false);

		    //when & then
			assertThatThrownBy(() -> authService.authenticate(authenticateUser))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 응답할_엑세스_토큰을_생성한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser authenticateUser = new AuthenticateUser(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findUserByLoginId(authenticateUser.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUser.password(), user.getPassword()))
				.thenReturn(true);
			Mockito.when(jwtUtil.createAccessToken(Mockito.any(User.class), Mockito.any()))
				.thenReturn("accessToken");

		    //when
			authService.authenticate(authenticateUser);

		    //then
			Mockito.verify(jwtUtil, Mockito.times(1))
				.createAccessToken(Mockito.any(), Mockito.any());

		}

		@Test
		void 응답할_재발급_토큰을_생성한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser authenticateUser = new AuthenticateUser(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findUserByLoginId(authenticateUser.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUser.password(), user.getPassword()))
				.thenReturn(true);
			Mockito.when(jwtUtil.createRefreshToken(Mockito.any(User.class), Mockito.any()))
				.thenReturn("refreshToken");

			//when
		    authService.authenticate(authenticateUser);

		    //then
		    Mockito.verify(jwtUtil, Mockito.times(1))
				.createRefreshToken(Mockito.any(), Mockito.any());
		}

		@Test
		void 인증된_사용자의_재발급_토큰을_저장한다() {
		    //given
			String refreshToken = "refreshToken";

			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser authenticateUser = new AuthenticateUser(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findUserByLoginId(authenticateUser.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUser.password(), user.getPassword()))
				.thenReturn(true);
			Mockito.when(jwtUtil.createRefreshToken(Mockito.any(User.class), Mockito.any()))
				.thenReturn(refreshToken);

		    //when
		    authService.authenticate(authenticateUser);

		    //then
			Mockito.verify(refreshTokenStore, Mockito.times(1))
				.save(authenticateUser.loginId(), refreshToken);

		}

		@Test
		void 인증을_한다() {
		    //given
			String accessToken = "accessToken";
			String refreshToken = "refreshToken";

			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUser authenticateUser = new AuthenticateUser(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findUserByLoginId(authenticateUser.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUser.password(), user.getPassword()))
				.thenReturn(true);
			Mockito.when(jwtUtil.createAccessToken(Mockito.any(User.class), Mockito.any()))
				.thenReturn(accessToken);
			Mockito.when(jwtUtil.createRefreshToken(Mockito.any(User.class), Mockito.any()))
				.thenReturn(refreshToken);

		    //when
			AuthenticationResult result = authService.authenticate(authenticateUser);

			//then
			assertAll(
				() -> Assertions.assertThat(result.accessToken()).isEqualTo(accessToken),
				() -> Assertions.assertThat(result.refreshToken()).isEqualTo(refreshToken)
			);
		}
	}

	@Nested
	@DisplayName("인증 무효화(로그아웃) 기능")
	class Invalidate {
		@Test
		void 인증_정보를_삭제한다() {
		    //given
			String loginId = "loginId";

		    //when
		    authService.invalidate(loginId);

		    //then
			Mockito.verify(refreshTokenStore, Mockito.times(1))
				.delete(loginId);
		}
	}

	@Nested
	@DisplayName("토큰 재발급 기능")
	class ReissueToken {
		@Test
		void 재발급_토큰으로_Subject를_추출한다() {
		    //given
			String subject = "subject";
			String refreshToken = "refreshToken";

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(subject);

			Mockito.when(jwtUtil.getRefreshTokenClaims(refreshToken)).thenReturn(claims);

		    //when & then
			Assertions.assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(UnauthorizedException.class);

			Mockito.verify(jwtUtil, Mockito.times(1)).getRefreshTokenClaims(refreshToken);
		}

		@Test
		void Subject로_저장된_재발급_토큰을_조회한다() {
		    //given
			String subject = "subject";
			String refreshToken = "refreshToken";

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(subject);
			Mockito.when(jwtUtil.getRefreshTokenClaims(refreshToken)).thenReturn(claims);

			Mockito.when(refreshTokenStore.get(subject))
				.thenReturn("refreshToken");

		    //when & then
			assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(UnauthorizedException.class);

			Mockito.verify(refreshTokenStore, Mockito.times(1))
				.get(subject);
		}

		@Test
		void 저장된_토큰과_요청된_토큰이_다르다면_예외를_반환한다() {
		    //given
		    String subject = "subject";
			String refreshToken = "wrongRefreshToken";

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(subject);
			Mockito.when(jwtUtil.getRefreshTokenClaims(refreshToken)).thenReturn(claims);

			Mockito.when(refreshTokenStore.get(subject))
				.thenReturn("refreshToken");

		    //when & then
			assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 저장된_토큰과_요청된_토큰이_다르다면_저장된_토큰을_삭제한다() {
			//given
			String subject = "subject";
			String refreshToken = "wrongRefreshToken";

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(subject);
			Mockito.when(jwtUtil.getRefreshTokenClaims(refreshToken)).thenReturn(claims);

			Mockito.when(refreshTokenStore.get(subject))
				.thenReturn("storedRefreshToken");

			//when & then
			assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(UnauthorizedException.class);

			Mockito.verify(refreshTokenStore, Mockito.times(1))
				.delete(subject);
		}

		@Test
		void Subject로_사용자를_조회한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			String subject = user.getLoginId();
			String refreshToken = "refreshToken";

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(subject);
			Mockito.when(jwtUtil.getRefreshTokenClaims(refreshToken)).thenReturn(claims);

			Mockito.when(refreshTokenStore.get(subject))
				.thenReturn(refreshToken);
			Mockito.when(userRepository.findUserByLoginId(subject))
				.thenReturn(Optional.of(user));

			//when
			authService.reissueToken(refreshToken);

			//then
			Mockito.verify(userRepository, Mockito.times(1))
				.findUserByLoginId(subject);
		}

		@Test
		void Subject에_맞는_사용자가_없는_경우_예외를_반환한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			String subject = user.getLoginId();
			String refreshToken = "refreshToken";

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(subject);
			Mockito.when(jwtUtil.getRefreshTokenClaims(refreshToken)).thenReturn(claims);

			Mockito.when(refreshTokenStore.get(subject))
				.thenReturn(refreshToken);
			Mockito.doThrow(UnauthorizedException.class)
				.when(userRepository).findUserByLoginId(subject);

			//when & then
			assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(UnauthorizedException.class);
		}

		@Test
		void 토큰을_재발급한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			String subject = user.getLoginId();
			String refreshToken = "refreshToken";

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(subject);
			Mockito.when(jwtUtil.getRefreshTokenClaims(refreshToken)).thenReturn(claims);

			Mockito.when(refreshTokenStore.get(subject))
				.thenReturn(refreshToken);
			Mockito.when(userRepository.findUserByLoginId(subject))
				.thenReturn(Optional.of(user));

			String newAccessToken = "newAccessToken";
			Mockito.when(jwtUtil.createAccessToken(Mockito.any(User.class), Mockito.any()))
				.thenReturn(newAccessToken);

			//when
			ReissuedToken result = authService.reissueToken(refreshToken);

			//then
			Assertions.assertThat(result.accessToken()).isEqualTo(newAccessToken);
		}
	}
}