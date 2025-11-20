package com.hello.boilerplate.domain.user.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.hello.boilerplate.domain.auth.service.RedisTokenService;
import com.hello.boilerplate.domain.user.domain.UserWithdrawnEvent;
import com.hello.module.IntegrationSupportTest;

class UserWithdrawnEventHandlerTest extends IntegrationSupportTest {

	@Autowired
	UserWithdrawnEventHandler userWithdrawnEventHandler;

	@MockitoBean
	RedisTokenService redisTokenService;

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
			Mockito.verify(redisTokenService).deleteRefreshToken(loginId);
		}
	}
}