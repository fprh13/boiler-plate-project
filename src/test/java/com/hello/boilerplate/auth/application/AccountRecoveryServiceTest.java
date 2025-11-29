package com.hello.boilerplate.auth.application;


import static org.assertj.core.api.Assertions.*;

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

import com.hello.boilerplate.auth.exception.AuthorizationErrorMessages;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.auth.infrastructure.verification.VerificationPurpose;
import com.hello.boilerplate.auth.presentation.dto.request.FindLoginId;
import com.hello.boilerplate.auth.presentation.dto.request.FindPassword;
import com.hello.boilerplate.auth.presentation.dto.request.ResetPassword;
import com.hello.boilerplate.auth.presentation.dto.request.VerifyPasswordCode;
import com.hello.boilerplate.auth.presentation.dto.response.PasswordCodeVerified;
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
class AccountRecoveryServiceTest {
	@InjectMocks AccountRecoveryService accountRecoveryService;
	@Mock UserRepository userRepository;
	@Mock TemplateRenderer templateRenderer;
	@Mock MailSender mailSender;
	@Mock VerificationCodeStore verificationCodeStore;
	@Mock JwtUtil jwtUtil;
	@Mock BCryptPasswordEncoder bCryptPasswordEncoder;

	@Nested
	@DisplayName("아이디 찾기 기능")
	class RetrieveLoginId {
		@Test
		void 이메일에_해당하는_사용자가_있는지_확인한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindLoginId findLoginId = new FindLoginId(user.getEmail());

			Mockito.when(userRepository.findByEmail(findLoginId.email())).thenReturn(Optional.of(user));

			//when
			accountRecoveryService.retrieveLoginId(findLoginId);

			//then
			Mockito.verify(userRepository, Mockito.times(1)).findByEmail(findLoginId.email());
		}

		@Test
		void 이메일에_해당하는_사용자가_없다면_예외를_반환한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindLoginId findLoginId = new FindLoginId(user.getEmail());

			Mockito.when(userRepository.findByEmail(findLoginId.email())).thenReturn(Optional.empty());

			//when & then
			assertThatThrownBy(() -> accountRecoveryService.retrieveLoginId(findLoginId))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 요청된_이메일로_메일_템플릿을_작성한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			FindLoginId findLoginId = new FindLoginId(user.getEmail());

			Mockito.when(userRepository.findByEmail(findLoginId.email())).thenReturn(Optional.of(user));
			Mockito.when(templateRenderer.render(Mockito.any(), Mockito.any()))
				.thenReturn("mailContent");
			//when
			accountRecoveryService.retrieveLoginId(findLoginId);

			//then
			Mockito.verify(templateRenderer, Mockito.times(1))
				.render(Mockito.any(), Mockito.any());
		}

		@Test
		void 아이디_찾기_이메일을_전송한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			FindLoginId findLoginId = new FindLoginId(user.getEmail());
			String mailContent = "mailContent";

			Mockito.when(userRepository.findByEmail(findLoginId.email())).thenReturn(Optional.of(user));
			Mockito.when(templateRenderer.render(Mockito.any(), Mockito.any()))
				.thenReturn(mailContent);

			//when
			accountRecoveryService.retrieveLoginId(findLoginId);

			//then
			Mockito.verify(mailSender, Mockito.times(1))
				.send(Mockito.any(), Mockito.any(), Mockito.any());
		}
	}

	@Nested
	@DisplayName("비밀 번호 찾기 기능")
	class RetrievePassword {
		@Test
		void 로그인_아이디로_사용자를_조회한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindPassword findPassword = new FindPassword(user.getLoginId(), user.getEmail());

			Mockito.when(userRepository.findByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
				.thenReturn(Optional.of(user));

			//when
			accountRecoveryService.retrievePassword(findPassword);

			//then
			Mockito.verify(userRepository, Mockito.times(1))
				.findByLoginIdAndEmail(findPassword.loginId(), findPassword.email());
		}

		@Test
		void 로그인_아이디에_맞는_사용자가_없으면_예외를_반환한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindPassword findPassword = new FindPassword(user.getLoginId(), user.getEmail());

			Mockito.when(userRepository.findByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
				.thenReturn(Optional.empty());

			//when & then
			Assertions.assertThatThrownBy(() -> accountRecoveryService.retrievePassword(findPassword))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 비밀번호_찾기_인증번호를_저장한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindPassword findPassword = new FindPassword(user.getLoginId(), user.getEmail());

			Mockito.when(userRepository.findByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
				.thenReturn(Optional.of(user));
			Mockito.doNothing().when(verificationCodeStore).save(Mockito.any(),Mockito.any(), Mockito.any());

			//when
			accountRecoveryService.retrievePassword(findPassword);

			//then
			Mockito.verify(verificationCodeStore, Mockito.times(1))
				.save(Mockito.any(),Mockito.any(), Mockito.any());
		}

		@Test
		void 비밀번호_찾기_템플릿을_작성한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindPassword findPassword = new FindPassword(user.getLoginId(), user.getEmail());

			Mockito.when(userRepository.findByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
				.thenReturn(Optional.of(user));
			Mockito.doNothing().when(verificationCodeStore).save(Mockito.any(),Mockito.any(), Mockito.any());
			Mockito.when(templateRenderer.render(Mockito.any(), Mockito.any()))
				.thenReturn("mailContent");

			//when
			accountRecoveryService.retrievePassword(findPassword);

			//then
			Mockito.verify(templateRenderer, Mockito.times(1))
				.render(Mockito.any(), Mockito.any());
		}

		@Test
		void 비밀번호_찾기_이메일을_전송한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindPassword findPassword = new FindPassword(user.getLoginId(), user.getEmail());

			Mockito.when(userRepository.findByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
				.thenReturn(Optional.of(user));
			Mockito.doNothing().when(verificationCodeStore).save(Mockito.any(),Mockito.any(), Mockito.any());
			Mockito.when(templateRenderer.render(Mockito.any(), Mockito.any()))
				.thenReturn("mailContent");
			Mockito.doNothing().when(mailSender).send(Mockito.any(), Mockito.any(), Mockito.any());

			//when
			accountRecoveryService.retrievePassword(findPassword);

			//then
			Mockito.verify(mailSender, Mockito.times(1))
				.send(Mockito.any(), Mockito.any(), Mockito.any());
		}
	}

	@Nested
	@DisplayName("인증 번호 검증 기능")
	class VerifyCode {
		@Test
		void 인증_번호를_조회한다() {
		    //given
			String code = "123456";
			User user = UserFixture.USER_FIXTURE_1.create();
			VerifyPasswordCode verifyPasswordCode = new VerifyPasswordCode(user.getLoginId(), code);

			Mockito.when(verificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, verifyPasswordCode.loginId()))
				.thenReturn(code);

			//when
		    accountRecoveryService.verifyCode(verifyPasswordCode);

		    //then
		    Mockito.verify(verificationCodeStore, Mockito.times(1))
				.get(VerificationPurpose.PASSWORD_RESET, verifyPasswordCode.loginId());
		}

		@Test
		void 인증번호가_올바르지_않다면_예외를_반환한다() {
		    //given
			String code = "123456";
			User user = UserFixture.USER_FIXTURE_1.create();
			VerifyPasswordCode verifyPasswordCode = new VerifyPasswordCode(user.getLoginId(), code);

			String otherCode = "654321";
			Mockito.when(verificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, verifyPasswordCode.loginId()))
				.thenReturn(otherCode);

		    //when & then
			Assertions.assertThatThrownBy(() -> accountRecoveryService.verifyCode(verifyPasswordCode))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 사용된_인증번호는_삭제한다() {
		    //given
			String code = "123456";
			User user = UserFixture.USER_FIXTURE_1.create();
			VerifyPasswordCode verifyPasswordCode = new VerifyPasswordCode(user.getLoginId(), code);

			Mockito.when(verificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, verifyPasswordCode.loginId()))
				.thenReturn(code);
			Mockito.doNothing().when(verificationCodeStore).delete(Mockito.any(), Mockito.any());

		    //when
			accountRecoveryService.verifyCode(verifyPasswordCode);

		    //then
		    Mockito.verify(verificationCodeStore, Mockito.times(1))
				.delete(Mockito.any(), Mockito.any());
		}

		@Test
		void 인증_토큰을_발행한다() {
		    //given
			String code = "123456";
			User user = UserFixture.USER_FIXTURE_1.create();
			VerifyPasswordCode verifyPasswordCode = new VerifyPasswordCode(user.getLoginId(), code);

			Mockito.when(verificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, verifyPasswordCode.loginId()))
				.thenReturn(code);
			Mockito.doNothing().when(verificationCodeStore).delete(Mockito.any(), Mockito.any());

			String token = "testToken";
			Mockito.when(jwtUtil.createVerificationToken(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(token);

		    //when
			accountRecoveryService.verifyCode(verifyPasswordCode);

		    //then
		    Mockito.verify(jwtUtil, Mockito.times(1))
				.createVerificationToken(Mockito.any(), Mockito.any(), Mockito.any());
		}

		@Test
		void 인증_번호를_검증한다() {
		    //given
			String code = "123456";
			User user = UserFixture.USER_FIXTURE_1.create();
			VerifyPasswordCode verifyPasswordCode = new VerifyPasswordCode(user.getLoginId(), code);

			Mockito.when(verificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, verifyPasswordCode.loginId()))
				.thenReturn(code);
			Mockito.doNothing().when(verificationCodeStore).delete(Mockito.any(), Mockito.any());

			String token = "testToken";
			Mockito.when(jwtUtil.createVerificationToken(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(token);

		    //when
			PasswordCodeVerified passwordCodeVerified = accountRecoveryService.verifyCode(verifyPasswordCode);

			//then
		    Assertions.assertThat(passwordCodeVerified.token()).isEqualTo(token);
		}
	}

	@Nested
	@DisplayName("비밀번호 초기화 기능")
	class ResetPasswordByVerificationToken {
		@Test
		void 아이디로_회원을_조회한다() {
		    //given
			String loginId = "testLoginId";
			String token = "testToken";
			String password = "test@1234";
			ResetPassword resetPassword = new ResetPassword(token, password);

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(loginId);

			Mockito.when(jwtUtil.getVerificationToken(VerificationPurpose.PASSWORD_RESET, token))
				.thenReturn(claims);

			Mockito.when(userRepository.findByLoginId(claims.getSubject()))
				.thenReturn(Optional.of(UserFixture.USER_FIXTURE_1.create()));

		    //when
		    accountRecoveryService.resetPasswordByVerificationToken(resetPassword);

		    //then
		    Mockito.verify(userRepository, Mockito.times(1))
				.findByLoginId(claims.getSubject());
		}

		@Test
		void 아이디에_해당하는_회원이_없다면_예외를_반환한다() {
		    //given
			String loginId = "testLoginId";
			String token = "testToken";
			String password = "test@1234";
			ResetPassword resetPassword = new ResetPassword(token, password);

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(loginId);

			Mockito.when(jwtUtil.getVerificationToken(VerificationPurpose.PASSWORD_RESET, token))
				.thenReturn(claims);

			Mockito.when(userRepository.findByLoginId(claims.getSubject()))
				.thenThrow(new UnauthorizedException(AuthorizationErrorMessages.AUTH_USER_NOT_FOUND));

		    //when & then
			Assertions.assertThatThrownBy(() -> accountRecoveryService.resetPasswordByVerificationToken(resetPassword))
				.isInstanceOf(UnauthorizedException.class);

		}

		@Test
		void 새로운_비밀번호로_초기화한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			String token = "testToken";
			String password = "test@1234";
			ResetPassword resetPassword = new ResetPassword(token, password);

			Claims claims = Mockito.mock(Claims.class);
			Mockito.when(claims.getSubject()).thenReturn(user.getLoginId());

			Mockito.when(jwtUtil.getVerificationToken(VerificationPurpose.PASSWORD_RESET, token))
				.thenReturn(claims);

			Mockito.when(userRepository.findByLoginId(claims.getSubject()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.encode(password)).thenReturn(password);

		    //when
		    accountRecoveryService.resetPasswordByVerificationToken(resetPassword);

		    //then
		    Assertions.assertThat(user.getPassword()).isEqualTo(password);
		}
	}
}