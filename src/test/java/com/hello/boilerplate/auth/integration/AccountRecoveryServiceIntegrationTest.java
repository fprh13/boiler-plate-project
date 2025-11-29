package com.hello.boilerplate.auth.integration;

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.hello.boilerplate.auth.application.AccountRecoveryService;
import com.hello.boilerplate.auth.application.VerificationCodeStore;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.auth.infrastructure.verification.VerificationPurpose;
import com.hello.boilerplate.auth.presentation.dto.request.RetrievePasswordRequest;
import com.hello.boilerplate.auth.presentation.dto.request.ResetPasswordRequest;
import com.hello.boilerplate.auth.presentation.dto.request.VerifyPasswordCodeRequest;
import com.hello.boilerplate.auth.presentation.dto.response.VerifyPasswordCodeResponse;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.NotFoundException;
import com.hello.boilerplate.common.exception.UnauthorizedException;
import com.hello.boilerplate.common.infrastructure.mail.MailSender;
import com.hello.boilerplate.common.infrastructure.mail.TemplateRenderer;
import com.hello.boilerplate.support.IntegrationSupportTest;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;

public class AccountRecoveryServiceIntegrationTest extends IntegrationSupportTest {

	@Autowired AccountRecoveryService accountRecoveryService;
	@Autowired UserRepository userRepository;
	@Autowired BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired VerificationCodeStore verificationCodeStore;
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
	@DisplayName("아이디 찾기 기능")
	class RetrieveLoginIdRequest {
		@Test
		void 아이디_찾기_이메일을_전송한다() {
			//given
			String email = user.getEmail();
			com.hello.boilerplate.auth.presentation.dto.request.RetrieveLoginIdRequest retrieveLoginIdRequest = new com.hello.boilerplate.auth.presentation.dto.request.RetrieveLoginIdRequest(email);

			//when
			accountRecoveryService.retrieveLoginId(retrieveLoginIdRequest);

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
			com.hello.boilerplate.auth.presentation.dto.request.RetrieveLoginIdRequest retrieveLoginIdRequest = new com.hello.boilerplate.auth.presentation.dto.request.RetrieveLoginIdRequest(email);

			//when & then
			Assertions.assertThatThrownBy(() -> accountRecoveryService.retrieveLoginId(retrieveLoginIdRequest))
				.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("비밀 번호 찾기 기능")
	class RetrievePassword {
		@Test
		void 비밀번호_찾기_이메일을_전송한다() {
			//given
			RetrievePasswordRequest retrievePasswordRequest = new RetrievePasswordRequest(user.getLoginId(), user.getEmail());

			//when
			accountRecoveryService.retrievePassword(retrievePasswordRequest);

			//then
			Mockito.verify(templateRenderer, Mockito.times(1))
				.render(Mockito.any(), Mockito.any());
			Mockito.verify(mailSender, Mockito.times(1))
				.send(Mockito.any(), Mockito.any(), Mockito.any());

			Assertions.assertThat(verificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, retrievePasswordRequest.loginId())).isInstanceOf(String.class);
		}

		@Test
		void 요청_데이터와_일치하는_사용자가_없으면_예외를_반환한다() {
			//given
			User otherUser = UserFixture.USER_FIXTURE_2.create();
			RetrievePasswordRequest retrievePasswordRequest = new RetrievePasswordRequest(otherUser.getLoginId(), otherUser.getEmail());

			//when
			Assertions.assertThatThrownBy(() -> accountRecoveryService.retrievePassword(retrievePasswordRequest))
				.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("인증 번호 검증 기능")
	class VerifyCode {
		@Test
		void 인증번호를_검증한다() {
		    //given
			String loginId = user.getLoginId();
			String code = "123456";
			verificationCodeStore.save(VerificationPurpose.PASSWORD_RESET, loginId, code);
			VerifyPasswordCodeRequest verifyPasswordCodeRequest = new VerifyPasswordCodeRequest(loginId, code);

			//when
			VerifyPasswordCodeResponse verifyPasswordCodeResponse = accountRecoveryService.verifyCode(verifyPasswordCodeRequest);

			//then
		    Assertions.assertThat(verifyPasswordCodeResponse.token()).isNotNull();
		}

		@Test
		void 인증번호가_올바르지_않은_경우_예외를_반환한다() {
		    //given
			String loginId = user.getLoginId();
			String code = "123456";
			verificationCodeStore.save(VerificationPurpose.PASSWORD_RESET, loginId, code);

			String wrongCode = "654321";
			VerifyPasswordCodeRequest verifyPasswordCodeRequest = new VerifyPasswordCodeRequest(loginId, wrongCode);

		    //when & then
			Assertions.assertThatThrownBy(() -> accountRecoveryService.verifyCode(verifyPasswordCodeRequest))
				.isInstanceOf(CustomException.class);
		}
	}

	@Nested
	@DisplayName("비밀번호 초기화 기능")
	class ResetPasswordRequestByVerificationToken {
		@Test
		void 비밀번호를_초기화_한다() {
		    //given
			String token = jwtUtil.createVerificationToken(
				VerificationPurpose.PASSWORD_RESET,
				user.getLoginId(),
				new Date()
			);
			String password = "test@1234";
			ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(token, password);

			//when
		    accountRecoveryService.resetPasswordByVerificationToken(resetPasswordRequest);

		    //then
		    Assertions.assertThat(bCryptPasswordEncoder.matches(password, user.getPassword())).isTrue();
		}

		@Test
		void 토큰에_해당하는_사용자가_없다면_예외를_반환한다() {
		    //given
			String token = jwtUtil.createVerificationToken(
				VerificationPurpose.PASSWORD_RESET,
				"wrongLoginId",
				new Date()
			);
			String password = "test@1234";
			ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(token, password);

		    //when & then
			Assertions.assertThatThrownBy(() -> accountRecoveryService.resetPasswordByVerificationToken(
					resetPasswordRequest))
				.isInstanceOf(UnauthorizedException.class);
		}
	}
}
