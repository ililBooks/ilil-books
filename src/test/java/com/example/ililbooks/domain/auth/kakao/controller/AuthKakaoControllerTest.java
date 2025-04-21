package com.example.ililbooks.domain.auth.kakao.controller;

import com.example.ililbooks.client.kakao.dto.AuthKakaoTokenResponse;
import com.example.ililbooks.domain.auth.kakao.service.AuthKakaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthKakaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthKakaoService authKakaoService;

    private final String accessToken = "access-token";
    private final String refreshToken = "refresh-token";

    @Test
    void 카카오_로그인_성공시_refreshToken은_쿠키에만_저장된다() throws Exception {
        // given
        AuthKakaoTokenResponse responseDto = new AuthKakaoTokenResponse(accessToken, refreshToken);

        given(authKakaoService.signInWithKakao(anyString()))
                .willReturn(responseDto);

        // when
        MvcResult result = mockMvc.perform(get("/api/v1/auth/kakao/token")
                        .param("code", "dummy-code"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken=");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/");
    }
}