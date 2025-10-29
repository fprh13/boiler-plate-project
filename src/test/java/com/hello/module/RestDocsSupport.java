package com.hello.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hello.boilerplate.domain.auth.config.SecurityConfig;
import com.hello.boilerplate.domain.auth.exception.AccessDeniedHandlerImpl;
import com.hello.boilerplate.domain.auth.exception.AuthenticationEntryPointImpl;
import com.hello.boilerplate.domain.auth.utils.JwtUtil;
import com.hello.boilerplate.domain.user.controller.UserController;
import com.hello.boilerplate.domain.user.repository.UserRepository;
import com.hello.boilerplate.domain.user.service.UserService;
import com.hello.boilerplate.global.controller.HealthCheckController;
import com.hello.boilerplate.support.config.RestDocsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        HealthCheckController.class,
        UserController.class
})
@Import({
        SecurityConfig.class,
        RestDocsConfig.class
})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public abstract class RestDocsSupport {

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationEntryPointImpl authenticationEntryPoint;

    @MockitoBean
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected UserService userService;


}
