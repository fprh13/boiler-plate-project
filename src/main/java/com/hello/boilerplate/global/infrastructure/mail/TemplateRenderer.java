package com.hello.boilerplate.global.infrastructure.mail;

import java.util.Map;

public interface TemplateRenderer {
	String render(String templateName, Map<String, Object> model);
}
