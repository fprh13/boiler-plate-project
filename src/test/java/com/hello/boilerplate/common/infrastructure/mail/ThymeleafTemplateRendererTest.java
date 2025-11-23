package com.hello.boilerplate.common.infrastructure.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class ThymeleafTemplateRendererTest {

	@InjectMocks
	private ThymeleafTemplateRenderer templateRenderer;

	@Mock
	private SpringTemplateEngine templateEngine;

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
		String result = templateRenderer.render(templateName, model);

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