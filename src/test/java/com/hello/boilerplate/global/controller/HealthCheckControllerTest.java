package com.hello.boilerplate.global.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.hello.module.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HealthCheckControllerTest extends RestDocsSupport {

    @Test
    void API_HealthCheck() throws Exception {
        //given & when
        ResultActions actions = mockMvc.perform(get("/health"));

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("OK"))
                .andDo(restDocsHandler.document(
                        ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                .tag("Global")
                                .summary("서버 상태 체크")
                                .responseSchema(Schema.schema("Health"))
                                .description("- 서버가 살아있으면 OK")
                                .build())
                ));
    }
}