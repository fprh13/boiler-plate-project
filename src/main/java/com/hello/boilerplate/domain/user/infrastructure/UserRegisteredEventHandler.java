package com.hello.boilerplate.domain.user.infrastructure;

import java.util.Map;

import com.hello.boilerplate.domain.user.domain.UserRegisteredEvent;
import com.hello.boilerplate.global.infrastructure.MailSender;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRegisteredEventHandler {
    private static final String WELCOME_MAIL_SUBJECT = "회원가입에 감사드립니다";
	private static final String WELCOME_MAIL = "mail/user/welcome";

	private final MailSender mailSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("asyncThreadPool")
    public void onUserRegistered(UserRegisteredEvent event) {
		String emailContent = mailSender
			.renderTemplate(WELCOME_MAIL, Map.of("name", event.user().getName()));

		mailSender.sendMail(event.user().getEmail(), WELCOME_MAIL_SUBJECT, emailContent);
	}
}
