package com.hello.boilerplate.common.infrastructure.mail;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpMailSender implements MailSender {

	@Value("${spring.mail.address}")
	private String senderAddress;

	private final JavaMailSender javaMailSender;

	@Override
	public void send(String recipientAddress, String mailSubject, String mailContent) {
		MimeMessage message = javaMailSender.createMimeMessage();
		try {
			message.addRecipients(MimeMessage.RecipientType.TO, recipientAddress);
			message.setSubject(MAIL_SUBJECT_PREFIX + mailSubject);
			message.setText(mailContent, MAIL_CHARSET, MAIL_SUBTYPE_HTML);

			message.setFrom(new InternetAddress(senderAddress, DOMAIN_NAME));
			javaMailSender.send(message);

		} catch (MailException e) {
			log.error("메일 전송 중 오류가 발생했습니다. {}", e.getMessage());
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.error("메일 세팅 중 오류가 발생했습니다. {}", e.getMessage());
		}
	}
}