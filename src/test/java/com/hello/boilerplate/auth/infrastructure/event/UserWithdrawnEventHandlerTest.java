package com.hello.boilerplate.auth.infrastructure.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.hello.boilerplate.auth.application.RefreshTokenStore;
import com.hello.boilerplate.user.domain.UserWithdrawnEvent;
import com.hello.boilerplate.support.IntegrationSupportTest;

class UserWithdrawnEventHandlerTest extends IntegrationSupportTest {

	@Autowired
	UserWithdrawnEventHandler userWithdrawnEventHandler;

	@MockitoBean
	RefreshTokenStore redisTokenService;

	@Nested
	@DisplayName("회원 탈퇴 이벤트 발생")
	class onUserWithdrawn {

		@Test
		 void 회원_토큰을_삭제한다() {
		    //given
		    String loginId = "testLoginId";
			UserWithdrawnEvent event = new UserWithdrawnEvent(loginId);

		    //when
		    userWithdrawnEventHandler.onUserWithdrawn(event);

		    //then
			Mockito.verify(redisTokenService).delete(loginId);
		}
	}
}