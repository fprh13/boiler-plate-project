package com.hello.boilerplate.global.application;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailService {
	private static final String DOMAIN_NAME = "BPCOM";
	private static final String MAIL_CHARSET = "utf-8";
	private static final String MAIL_SUBTYPE = "html";

	private final String senderAddress;
	private final JavaMailSender mailSender;

	public MailService(JavaMailSender mailSender,
		@Value("${spring.mail.host}") String HOST_ADDRESS,
		@Value("${spring.mail.username}") String MAIL_ADDRESS) {
		this.mailSender = mailSender;
		this.senderAddress = MAIL_ADDRESS + "@" + HOST_ADDRESS.replace("smtp.", "");
	}

	public void sendMail(String recipientAddress, String mailSubject, String mailContent) {
		MimeMessage message = mailSender.createMimeMessage();
		try {
			message.addRecipients(MimeMessage.RecipientType.TO, recipientAddress);
			message.setSubject(mailSubject);
			message.setText(mailContent, MAIL_CHARSET, MAIL_SUBTYPE);

			message.setFrom(new InternetAddress(senderAddress, DOMAIN_NAME));
			mailSender.send(message);

		} catch (MailException e) {
			log.error("메일 전송 중 오류가 발생했습니다. {}", e.getMessage());
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.error("메일 세팅 중 오류가 발생했습니다. {}", e.getMessage());
		}
	}
}