package com.hello.boilerplate.domain.user.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.hello.boilerplate.domain.user.dto.UserRequestDto;
import com.hello.boilerplate.domain.user.entity.User;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.module.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends RestDocsSupport {

    @Test
    void API_회원가입() throws Exception {
        //given
        User userFixture = UserFixture.USER_FIXTURE_1.create();
        Mockito.when(userService.register(any(UserRequestDto.Register.class)))
                .thenReturn(any(Long.class));
        UserRequestDto.Register requestDto = new UserRequestDto.Register(
                userFixture.getLoginId(),
                userFixture.getPassword(),
                userFixture.getEmail(),
                userFixture.getName()
        );

        //when
        ResultActions actions = mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("CREATED"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.errorCode").isEmpty())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andDo(restDocsHandler.document(
                        ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                .tag("User")
                                .summary("회원 가입")
                                .description("- 회원가입 입니다.")
                                .requestSchema(Schema.schema("UserRequestDto.Register"))
                                .responseSchema(Schema.schema("ResponseDto"))
                                .responseFields(
                                        fieldWithPath("statusCode").description("상태 코드").type(JsonFieldType.STRING),
                                        fieldWithPath("message").description("메세지").type(JsonFieldType.NULL),
                                        fieldWithPath("errorCode").description("에러코드").type(JsonFieldType.NULL),
                                        fieldWithPath("data").description("데이터").type(JsonFieldType.NUMBER)
                                ).build())
                ));
    }
}