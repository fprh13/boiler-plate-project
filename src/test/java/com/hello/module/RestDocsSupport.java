package com.hello.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hello.boilerplate.global.controller.HealthCheckController;
import com.hello.boilerplate.support.config.RestDocsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        HealthCheckController.class
})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfig.class)
public abstract class RestDocsSupport {

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
