package com.hello.boilerplate.common.infrastructure.logging;

import java.util.Arrays;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingFilter implements Filter {
	private static final String REQUEST_LOG_PREFIX = "[REQUEST] ";
	private static final String[] SKIP_PREFIXES = {
		"/actuator",
		"/swagger",
		"/v3/api-docs"
	};

	private final ExecutionTimeLogger executionTimeLogger;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
        HttpServletRequest req = (HttpServletRequest) request;
        try {
            if (isSkippableRequest(req)) {
                filterChain.doFilter(request, response);
                return;
            }

            String requestUri = generateMessage(req);
            log.info(requestUri);
            executionTimeLogger.execute(() -> filterChain.doFilter(request, response), requestUri);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	private boolean isSkippableRequest(HttpServletRequest req) {
		String uri = req.getRequestURI();
		return Arrays.stream(SKIP_PREFIXES)
			.anyMatch(uri::startsWith);
	}

    private String generateMessage(HttpServletRequest req) {
        Map<String, String[]> parameterMap = req.getParameterMap();
		StringBuilder message = new StringBuilder()
			.append(REQUEST_LOG_PREFIX)
			.append(req.getMethod())
			.append(" ")
			.append(req.getRequestURI());

		if (parameterMap.isEmpty()) {
			return message.toString();
		}

		message.append("?");

		parameterMap.forEach((key, values) ->
			message
				.append(key)
				.append("=")
				.append(Arrays.toString(values))
				.append("&")
		);

		message.setLength(message.length() - 1);

		return message.toString();
    }
}
