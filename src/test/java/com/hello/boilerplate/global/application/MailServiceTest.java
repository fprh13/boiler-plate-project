package com.hello.boilerplate.global.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {
	private static final String MAIL_SUBJECT_PREFIX = "[보일러플레이]";

	private MailService mailService;
	@Mock
	private JavaMailSender mailSender;
	@Mock
	private SpringTemplateEngine templateEngine;

	@BeforeEach
	void setUp() {
		mailService = new MailService(
			"test.com",
			"test",
			mailSender,
			templateEngine
		);
	}

	@Test
	void 이메일을_전송한다() {
	    //given
		String recipientAddress = "recipientAddress@test.com";
		String mailSubject = "테스트 메일 제목";
		String mailContent = "<h2>테스트 메일 내용</h2>";

		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

	    //when
		mailService.sendMail(recipientAddress, mailSubject, mailContent);

	    //then
		verify(mailSender).createMimeMessage();

		assertAll(
			() -> assertThat(mimeMessage.getSubject()).isEqualTo(MAIL_SUBJECT_PREFIX + mailSubject),
			() -> assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo(recipientAddress),
			() -> assertThat(mimeMessage.getContent()).isEqualTo(mailContent)
		);

		verify(mailSender).send(mimeMessage);
	}

	@Test
	void 메일전송_중_예외가_발생한다() {
		// given
		String recipientAddress = "recipientAddress@test.com";
		String mailSubject = "테스트 메일 제목";
		String mailContent = "<h2>테스트 메일 내용</h2>";

		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		doThrow(new MailSendException("SMTP 에러"))
			.when(mailSender).send(mimeMessage);

		// when & then
		// SMTP 예외로 예외를 던지지 않고 log로 남깁니다.
		assertDoesNotThrow(() ->
			mailService.sendMail(recipientAddress, mailSubject, mailContent)
		);

		// SMTP 예외로 send 자체는 실행되어야 합니다.
		verify(mailSender).send(mimeMessage);
	}

	@Test
	void 템플릿을_렌더링한다() {
		// given
		String templateName = "mail/user/welcome";
		String nameValue = "홍길동";
		Map<String, Object> model = Map.of("name", nameValue);
		String expectedHtml = "안녕하세요" + nameValue + "님";

		when(templateEngine.process(eq(templateName), any(Context.class)))
			.thenReturn(expectedHtml);

		// when
		String result = mailService.renderTemplate(templateName, model);

		// then
		ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq(templateName), captor.capture());
		Context context = captor.getValue();

		assertAll(
			() -> assertThat(result).isEqualTo(expectedHtml),
			() -> assertThat(context.getLocale()).isEqualTo(Locale.KOREA),
			() -> assertThat(context.getVariable("name")).isEqualTo("홍길동")
		);
	}

}