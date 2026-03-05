package com.bookhub.hyper_reserve.domain.auth.service;

import com.bookhub.hyper_reserve.domain.auth.dto.*;
import com.bookhub.hyper_reserve.domain.auth.repository.UserRepository;
import com.bookhub.hyper_reserve.entity.User;
import com.bookhub.hyper_reserve.global.config.JwtProvider;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    // ── 회원가입 테스트 ────────────────────────────────

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com", "Password1!", "홍길동"
        );
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded_pw");
        given(userRepository.save(any(User.class))).willReturn(
                User.builder()
                        .email(request.email())
                        .password("encoded_pw")
                        .name(request.name())
                        .build()
        );

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_duplicateEmail() {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com", "Password1!", "홍길동"
        );
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    // ── 로그인 테스트 ─────────────────────────────────

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "Password1!");
        User user = User.builder()
                .email(request.email())
                .password("encoded_pw")
                .name("홍길동")
                .role(User.Role.USER)
                .build();

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
        given(jwtProvider.generateAccessToken(any(), any())).willReturn("access_token");
        given(jwtProvider.generateRefreshToken(any())).willReturn("refresh_token");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("access_token");
        assertThat(response.refreshToken()).isEqualTo("refresh_token");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_fail_emailNotFound() {
        // given
        LoginRequest request = new LoginRequest("none@example.com", "Password1!");
        given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "WrongPass1!");
        User user = User.builder()
                .email(request.email())
                .password("encoded_pw")
                .name("홍길동")
                .role(User.Role.USER)
                .build();

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }
}