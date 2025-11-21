package com.hello.boilerplate.global.infrastructure.mail;

public interface MailSender {
	String MAIL_SUBJECT_PREFIX = "[보일러플레이]";
	String DOMAIN_NAME = "BPCOM";
	String MAIL_CHARSET = "utf-8";
	String MAIL_SUBTYPE_HTML = "html";

	void send(String recipientAddress, String mailSubject, String mailContent);
}
