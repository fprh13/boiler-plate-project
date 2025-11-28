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

import com.hello.boilerplate.auth.presentation.dto.request.FindLoginId;
import com.hello.boilerplate.auth.presentation.dto.request.FindPassword;
import com.hello.boilerplate.common.exception.NotFoundException;
import com.hello.boilerplate.common.infrastructure.mail.MailSender;
import com.hello.boilerplate.common.infrastructure.mail.TemplateRenderer;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;

@ExtendWith(MockitoExtension.class)
class AccountRecoveryServiceTest {
	@InjectMocks AccountRecoveryService accountRecoveryService;
	@Mock UserRepository userRepository;
	@Mock TemplateRenderer templateRenderer;
	@Mock MailSender mailSender;
	@Mock VerificationCodeStore verificationCodeStore;

	@Nested
	@DisplayName("아이디 찾기 기능")
	class RetrieveLoginId {
		@Test
		void 이메일에_해당하는_사용자가_있는지_확인한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindLoginId findLoginId = new FindLoginId(user.getEmail());

			Mockito.when(userRepository.findUserByEmail(findLoginId.email())).thenReturn(Optional.of(user));

			//when
			accountRecoveryService.retrieveLoginId(findLoginId);

			//then
			Mockito.verify(userRepository, Mockito.times(1)).findUserByEmail(findLoginId.email());
		}

		@Test
		void 이메일에_해당하는_사용자가_없다면_예외를_반환한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindLoginId findLoginId = new FindLoginId(user.getEmail());

			Mockito.when(userRepository.findUserByEmail(findLoginId.email())).thenReturn(Optional.empty());

			//when & then
			assertThatThrownBy(() -> accountRecoveryService.retrieveLoginId(findLoginId))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 요청된_이메일로_메일_템플릿을_작성한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			FindLoginId findLoginId = new FindLoginId(user.getEmail());

			Mockito.when(userRepository.findUserByEmail(findLoginId.email())).thenReturn(Optional.of(user));
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

			Mockito.when(userRepository.findUserByEmail(findLoginId.email())).thenReturn(Optional.of(user));
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

			Mockito.when(userRepository.findUserByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
				.thenReturn(Optional.of(user));

			//when
			accountRecoveryService.retrievePassword(findPassword);

			//then
			Mockito.verify(userRepository, Mockito.times(1))
				.findUserByLoginIdAndEmail(findPassword.loginId(), findPassword.email());
		}

		@Test
		void 로그인_아이디에_맞는_사용자가_없으면_예외를_반환한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();;
			FindPassword findPassword = new FindPassword(user.getLoginId(), user.getEmail());

			Mockito.when(userRepository.findUserByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
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

			Mockito.when(userRepository.findUserByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
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

			Mockito.when(userRepository.findUserByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
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

			Mockito.when(userRepository.findUserByLoginIdAndEmail(findPassword.loginId(), findPassword.email()))
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

}