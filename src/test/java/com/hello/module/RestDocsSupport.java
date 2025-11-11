package com.hello.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hello.boilerplate.domain.auth.config.SecurityConfig;
import com.hello.boilerplate.domain.auth.controller.AuthController;
import com.hello.boilerplate.domain.auth.exception.AccessDeniedHandlerImpl;
import com.hello.boilerplate.domain.auth.exception.AuthenticationEntryPointImpl;
import com.hello.boilerplate.domain.auth.resolver.AuthUserResolver;
import com.hello.boilerplate.domain.auth.service.AuthService;
import com.hello.boilerplate.domain.auth.utils.JwtUtil;
import com.hello.boilerplate.domain.user.presentation.UserController;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.domain.UserRepository;
import com.hello.boilerplate.domain.user.application.UserService;
import com.hello.boilerplate.global.controller.HealthCheckController;
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
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = {
        HealthCheckController.class,
        UserController.class,
        AuthController.class
})
@Import({
        SecurityConfig.class,
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

    @BeforeEach
    void setUp() {
        User userFixture = UserFixture.USER_FIXTURE_1.create();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userFixture.getLoginId(), null, List.of())
        );

        Mockito.when(userRepository.findUserByLoginId(userFixture.getLoginId()))
                .thenReturn(Optional.of(userFixture));
        Mockito.when(authUserResolver.resolveArgument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(userFixture);
    }

}
