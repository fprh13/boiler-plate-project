package com.hello.boilerplate.global.application;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailService {
	private static final String DOMAIN_NAME = "BPCOM";
	private static final String MAIL_SUBJECT_PREFIX = "[보일러플레이]";
	private static final String MAIL_CHARSET = "utf-8";
	private static final String MAIL_SUBTYPE = "html";

	private final String senderAddress;
	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;

	public MailService(
		@Value("${spring.mail.host}") String HOST_ADDRESS,
		@Value("${spring.mail.username}") String MAIL_ADDRESS,
		JavaMailSender mailSender,
		SpringTemplateEngine templateEngine
	) {
		this.senderAddress = MAIL_ADDRESS + "@" + HOST_ADDRESS.replace("smtp.", "");
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
	}

	public void sendMail(String recipientAddress, String mailSubject, String mailContent) {
		MimeMessage message = mailSender.createMimeMessage();
		try {
			message.addRecipients(MimeMessage.RecipientType.TO, recipientAddress);
			message.setSubject(MAIL_SUBJECT_PREFIX + mailSubject);
			message.setText(mailContent, MAIL_CHARSET, MAIL_SUBTYPE);

			message.setFrom(new InternetAddress(senderAddress, DOMAIN_NAME));
			mailSender.send(message);

		} catch (MailException e) {
			log.error("메일 전송 중 오류가 발생했습니다. {}", e.getMessage());
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.error("메일 세팅 중 오류가 발생했습니다. {}", e.getMessage());
		}
	}

	public String renderTemplate(String templateName, Map<String, Object> model) {
		return templateEngine.process(templateName, new Context(Locale.KOREA, model));
	}
}