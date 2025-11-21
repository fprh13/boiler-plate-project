package com.hello.boilerplate.global.infrastructure.mail;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class SmtpMailSenderTest {

	@InjectMocks
	private SmtpMailSender mailSender;

	@Mock
	private JavaMailSender javaMailSender;

	@Test
	void 이메일을_전송한다() {
	    //given
		String recipientAddress = "recipientAddress@test.com";
		String mailSubject = "테스트 메일 제목";
		String mailContent = "<h2>테스트 메일 내용</h2>";

		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	    //when
		mailSender.send(recipientAddress, mailSubject, mailContent);

	    //then
		verify(javaMailSender).createMimeMessage();

		assertAll(
			() -> assertThat(mimeMessage.getSubject()).isEqualTo(MailSender.MAIL_SUBJECT_PREFIX + mailSubject),
			() -> assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo(recipientAddress),
			() -> assertThat(mimeMessage.getContent()).isEqualTo(mailContent)
		);

		verify(javaMailSender).send(mimeMessage);
	}

	@Test
	void 메일전송_중_예외가_발생한다() {
		// given
		String recipientAddress = "recipientAddress@test.com";
		String mailSubject = "테스트 메일 제목";
		String mailContent = "<h2>테스트 메일 내용</h2>";

		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

		doThrow(new MailSendException("SMTP 에러"))
			.when(javaMailSender).send(mimeMessage);

		// when & then
		// SMTP 예외로 예외를 던지지 않고 log로 남깁니다.
		assertDoesNotThrow(() ->
			mailSender.send(recipientAddress, mailSubject, mailContent)
		);

		// SMTP 예외로 send 자체는 실행되어야 합니다.
		verify(javaMailSender).send(mimeMessage);
	}
}