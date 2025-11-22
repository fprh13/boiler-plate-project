package com.hello.boilerplate.user.infrastructure.event;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.hello.boilerplate.user.domain.UserRegisteredEvent;
import com.hello.boilerplate.common.infrastructure.mail.MailSender;
import com.hello.boilerplate.common.infrastructure.mail.TemplateRenderer;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.support.IntegrationSupportTest;

class UserRegisteredEventHandlerTest extends IntegrationSupportTest {

	@Autowired
	UserRegisteredEventHandler userRegisteredEventHandler;

	@MockitoBean
	MailSender mailSender;

	@MockitoBean
	TemplateRenderer templateRenderer;

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
			verify(templateRenderer).render(any(), any());

		}

		@Test
		void 환영_메일을_발송한다() {
		    //given
			UserRegisteredEvent event = new UserRegisteredEvent(UserFixture.USER_FIXTURE_1.create());

		    //when
			userRegisteredEventHandler.onUserRegistered(event);

		    //then
		    verify(mailSender).send(any(), any(), any());
		}
	}
}