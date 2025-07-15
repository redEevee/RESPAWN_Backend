package com.shop.respawn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginSessionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_ThenSessionCreated() throws Exception {
        // given: 회원 가입된 계정 사용
        String username = "testUser";
        String password = "testPassword";

        // 로그인 요청
        MvcResult result = mockMvc.perform(
                        SecurityMockMvcRequestBuilders.formLogin()
                                .user(username).password(password))
                .andExpect(status().is3xxRedirection()) // 성공시 리다이렉트
                .andReturn();

        // 세션이 정상 생성되는지 확인
        assertThat(result.getRequest().getSession(false)).isNotNull();
    }
}
