package com.hello.boilerplate.domain.user.infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.hello.boilerplate.domain.auth.service.RedisTokenService;
import com.hello.boilerplate.domain.user.domain.UserWithdrawnEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserWithdrawnEventHandler {

	private final RedisTokenService redisTokenService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onUserWithdrawn(UserWithdrawnEvent event) {
		redisTokenService.deleteRefreshToken(event.loginId());
	}
}
