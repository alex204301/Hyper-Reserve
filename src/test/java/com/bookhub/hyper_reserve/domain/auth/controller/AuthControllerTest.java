package com.bookhub.hyper_reserve.domain.auth.controller;

import com.bookhub.hyper_reserve.global.config.JwtProvider;
import com.bookhub.hyper_reserve.global.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookhub.hyper_reserve.domain.auth.dto.*;
import com.bookhub.hyper_reserve.domain.auth.service.AuthService;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean AuthService authService;
    @MockitoBean JwtProvider jwtProvider;

    // ── 회원가입 Controller 테스트 ─────────────────────

    @Test
    @DisplayName("회원가입 성공 - 201 반환")
    void signup_success() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com", "Password1!", "홍길동"
        );
        given(authService.signup(any())).willReturn(
                new SignupResponse(1L, "test@example.com", "홍길동")
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류 → 400")
    void signup_fail_invalidEmail() throws Exception {
        // given - 잘못된 이메일 형식
        String body = """
            {
                "email": "invalid-email",
                "password": "Password1!",
                "name": "홍길동"
            }
            """;

        // when & then
        // @Valid가 실패 → GlobalExceptionHandler가 잡아 400 반환
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 형식 오류 → 400")
    void signup_fail_invalidPassword() throws Exception {
        // given - 특수문자 없는 비밀번호
        String body = """
            {
                "email": "test@example.com",
                "password": "password1",
                "name": "홍길동"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이름 빈칸 오류 → 400")
    void signup_fail_noName() throws Exception {
        // given - 잘못된 이메일 형식
        String body = """
            {
                "email": "invalid-email",
                "password": "Password1!"
            }
            """;

        // when & then
        // @Valid가 실패 → GlobalExceptionHandler가 잡아 400 반환
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복 → 409")
    void signup_fail_duplicateEmail() throws Exception {
        // given - Service에서 BusinessException 던짐
        SignupRequest request = new SignupRequest(
                "test@example.com", "Password1!", "홍길동"
        );
        given(authService.signup(any()))
                .willThrow(new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS));

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    // ── 로그인 Controller 테스트 ───────────────────────

    @Test
    @DisplayName("로그인 성공 - 200 반환")
    void login_success() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "Password1!");
        given(authService.login(any())).willReturn(
                new LoginResponse("access_token", "refresh_token", 3600L)
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access_token"));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 필드 누락 → 400")
    void login_fail_missingEmail() throws Exception {
        // given - email 필드 없음
        String body = """
            {
                "password": "Password1!"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일/비밀번호 불일치 → 401")
    void login_fail_invalidCredentials() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "WrongPass1!");
        given(authService.login(any()))
                .willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }
}