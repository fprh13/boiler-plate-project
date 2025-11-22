package com.hello.boilerplate.auth.presentation;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.hello.boilerplate.auth.presentation.dto.request.LoginRequestDto;
import com.hello.boilerplate.auth.presentation.dto.response.LoginResponseDto;
import com.hello.boilerplate.auth.presentation.dto.response.ReissueResponseDto;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.support.RestDocsSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends RestDocsSupport {

    private static final String TEST_ACCESS_TOKEN = "accessabcdefghijklmnopqrstuvwxyz";
    private static final String TEST_REFRESH_TOKEN = "refreshabcdefghijklmnopqrstuvwxyz";

    @Test
    void 로그인_2XX() throws Exception {
        //given
        User userFixture = UserFixture.USER_FIXTURE_1.create();

        LoginRequestDto requestDto = new LoginRequestDto(userFixture.getLoginId(), userFixture.getPassword());
        LoginResponseDto responseDto = new LoginResponseDto(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);

        Mockito.when(authService.login(requestDto))
                .thenReturn(responseDto);

        //when
        ResultActions actions = mockMvc.perform(
                post("/auths/login")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(restDocsHandler.document(
                        ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .summary("로그인")
                                .description("- 로그인 입니다.")
                                .requestSchema(Schema.schema("LoginRequestDto"))
                                .responseSchema(Schema.schema("LoginResponseDto"))
                                .responseFields(
                                        fieldWithPath("message").description("메세지").type(JsonFieldType.STRING),
                                        fieldWithPath("data").description("데이터").type(JsonFieldType.NULL)
                                ).responseHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰"),
                                        headerWithName(HttpHeaders.SET_COOKIE).description("재발급 토큰")
                                ).build())
                        )
                );
    }

    @Test
    void 로그아웃_2XX() throws Exception {
        //given
        Mockito.doNothing().when(authService).logout(anyString());

        //when
        ResultActions actions = mockMvc.perform(
                post("/auths/logout")
                        .header(HttpHeaders.AUTHORIZATION, TEST_ACCESS_TOKEN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, TEST_REFRESH_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(restDocsHandler.document(
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("Auth")
                                        .summary("로그아웃")
                                        .description("- 로그아웃 입니다.")
                                        .requestHeaders(
                                                headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("message").description("메세지").type(JsonFieldType.STRING),
                                                fieldWithPath("data").description("데이터").type(JsonFieldType.NULL)
                                        ).responseHeaders(
                                                headerWithName(HttpHeaders.SET_COOKIE).description("쿠키 무효화")
                                        ).build())
                        )
                );
    }

    @Test
    void 재발급_2XX() throws Exception {
        //given
        ReissueResponseDto responseDto = new ReissueResponseDto(TEST_ACCESS_TOKEN);

        Mockito.when(authService.reissue(anyString(), anyString()))
                .thenReturn(responseDto);

        //when
        ResultActions actions = mockMvc.perform(
                post("/auths/reissue")
                        .header(HttpHeaders.AUTHORIZATION, TEST_ACCESS_TOKEN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, TEST_REFRESH_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(restDocsHandler.document(
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("Auth")
                                        .summary("재발급")
                                        .description("- 재발급 입니다.")
                                        .requestHeaders(
                                                headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("message").description("메세지").type(JsonFieldType.STRING),
                                                fieldWithPath("data").description("데이터").type(JsonFieldType.NULL)
                                        ).responseHeaders(
                                                headerWithName(HttpHeaders.AUTHORIZATION).description("엑세스 토큰")
                                        ).build())
                        )
                );
    }
}