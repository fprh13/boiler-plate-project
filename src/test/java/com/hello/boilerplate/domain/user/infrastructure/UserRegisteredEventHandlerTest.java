package com.hello.boilerplate.domain.user.infrastructure;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.hello.boilerplate.domain.user.domain.UserRegisteredEvent;
import com.hello.boilerplate.global.infrastructure.mail.MailClient;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.module.IntegrationSupportTest;

class UserRegisteredEventHandlerTest extends IntegrationSupportTest {

	@Autowired
	UserRegisteredEventHandler userRegisteredEventHandler;

	@MockitoBean
	MailClient mailClient;

	@Nested
	@DisplayName("회원 가입 이벤트 발행")
	class onUserRegistered {
		@Test
		void 환영_메일_템플릿을_작성한다() {
		    //given
		    UserRegisteredEvent event = new UserRegisteredEvent(UserFixture.USER_FIXTURE_1.create());

			//when
		    userRegisteredEventHandler.onUserRegistered(event);

		    //then
			verify(mailClient).renderTemplate(any(), any());

		}

		@Test
		void 환영_메일을_발송한다() {
		    //given
			UserRegisteredEvent event = new UserRegisteredEvent(UserFixture.USER_FIXTURE_1.create());

		    //when
			userRegisteredEventHandler.onUserRegistered(event);

		    //then
		    verify(mailClient).sendMail(any(), any(), any());
		}
	}
}