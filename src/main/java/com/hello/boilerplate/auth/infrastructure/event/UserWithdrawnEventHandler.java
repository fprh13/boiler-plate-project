package com.hello.boilerplate.auth.infrastructure.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.hello.boilerplate.auth.application.RefreshTokenStore;
import com.hello.boilerplate.user.domain.UserWithdrawnEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserWithdrawnEventHandler {

	private final RefreshTokenStore refreshTokenStore;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onUserWithdrawn(UserWithdrawnEvent event) {
		refreshTokenStore.delete(event.loginId());
	}
}
