package com.hello.boilerplate.auth.integration;

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
import com.hello.boilerplate.auth.infrastructure.verification.VerificationCodeType;
import com.hello.boilerplate.auth.presentation.dto.request.FindLoginId;
import com.hello.boilerplate.auth.presentation.dto.request.FindPassword;
import com.hello.boilerplate.common.exception.NotFoundException;
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
	class RetrieveLoginId {
		@Test
		void 아이디_찾기_이메일을_전송한다() {
			//given
			String email = user.getEmail();
			FindLoginId findLoginId = new FindLoginId(email);

			//when
			accountRecoveryService.retrieveLoginId(findLoginId);

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
			Assertions.assertThatThrownBy(() -> accountRecoveryService.retrieveLoginId(findLoginId))
				.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("비밀 번호 찾기 기능")
	class RetrievePassword {
		@Test
		void 비밀번호_찾기_이메일을_전송한다() {
			//given
			FindPassword findPassword = new FindPassword(user.getLoginId(), user.getEmail());

			//when
			accountRecoveryService.retrievePassword(findPassword);

			//then
			Mockito.verify(templateRenderer, Mockito.times(1))
				.render(Mockito.any(), Mockito.any());
			Mockito.verify(mailSender, Mockito.times(1))
				.send(Mockito.any(), Mockito.any(), Mockito.any());

			Assertions.assertThat(verificationCodeStore.get(VerificationCodeType.PASSWORD_RESET, findPassword.loginId())).isInstanceOf(String.class);
		}

		@Test
		void 요청_데이터와_일치하는_사용자가_없으면_예외를_반환한다() {
			//given
			User otherUser = UserFixture.USER_FIXTURE_2.create();;
			FindPassword findPassword = new FindPassword(otherUser.getLoginId(), otherUser.getEmail());

			//when
			Assertions.assertThatThrownBy(() -> accountRecoveryService.retrievePassword(findPassword))
				.isInstanceOf(NotFoundException.class);
		}
	}
}
