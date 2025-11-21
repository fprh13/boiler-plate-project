package com.hello.boilerplate.global.infrastructure.mail;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThymeleafTemplateRenderer implements TemplateRenderer {

	private final SpringTemplateEngine templateEngine;

	@Override
	public String render(String templateName, Map<String, Object> model) {
		return templateEngine.process(templateName, new Context(Locale.KOREA, model));
	}
}
