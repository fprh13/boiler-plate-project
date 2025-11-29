package com.hello.boilerplate.support;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hello.boilerplate.auth.application.AccountRecoveryService;
import com.hello.boilerplate.common.infrastructure.config.SecurityConfig;
import com.hello.boilerplate.auth.presentation.AuthController;
import com.hello.boilerplate.auth.infrastructure.security.AccessDeniedHandlerImpl;
import com.hello.boilerplate.auth.infrastructure.security.AuthenticationEntryPointImpl;
import com.hello.boilerplate.auth.presentation.resolver.AuthUserResolver;
import com.hello.boilerplate.auth.application.AuthService;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.common.infrastructure.logging.ExecutionTimeLogger;
import com.hello.boilerplate.user.presentation.UserController;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;
import com.hello.boilerplate.user.application.UserService;
import com.hello.boilerplate.common.presentation.HealthCheckController;
import com.hello.boilerplate.support.config.RestDocsConfig;
import com.hello.boilerplate.support.fixture.UserFixture;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = {
	HealthCheckController.class,
	UserController.class,
	AuthController.class
})
@Import({
	SecurityConfig.class,
	ExecutionTimeLogger.class,
	RestDocsConfig.class
})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public abstract class RestDocsSupport {

    @Value("${cookie.name}")
    protected String REFRESH_TOKEN_COOKIE_NAME;

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected AuthenticationEntryPointImpl authenticationEntryPoint;

    @MockitoBean
    protected AccessDeniedHandlerImpl accessDeniedHandler;

    @MockitoBean
    protected AuthUserResolver authUserResolver;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected AuthService authService;

	@MockitoBean
	protected AccountRecoveryService accountRecoveryService;

    @BeforeEach
    void setUp() {
        User userFixture = UserFixture.USER_FIXTURE_1.create();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userFixture.getLoginId(), null, List.of())
        );

        Mockito.when(userRepository.findByLoginId(userFixture.getLoginId()))
                .thenReturn(Optional.of(userFixture));
        Mockito.when(authUserResolver.resolveArgument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(userFixture);
    }

	protected String readMarkdown(String path) {
		ClassPathResource resource = new ClassPathResource(path);
		try {
			return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			fail("문서를 읽어들이는 도중 예외가 발생했습니다. : " + path);
			return null;
		}
	}
}
