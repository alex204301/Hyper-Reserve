package com.bookhub.hyper_reserve.domain.auth.service;

import com.bookhub.hyper_reserve.domain.auth.dto.*;
import com.bookhub.hyper_reserve.domain.auth.repository.UserRepository;
import com.bookhub.hyper_reserve.entity.User;
import com.bookhub.hyper_reserve.global.config.JwtProvider;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    // ── 회원가입 ───────────────────────────────────────
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 비밀번호 암호화 후 저장
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        User savedUser = userRepository.save(user);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName()
        );
    }

    // ── 로그인 ────────────────────────────────────────
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // 이메일로 유저 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // RefreshToken을 Redis에 저장 (7일)
        // key: "refresh:{userId}", value: refreshToken
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                7, TimeUnit.DAYS
        );

        return new LoginResponse(accessToken, refreshToken, 3600L);
    }

    // ── 토큰 재발급 ───────────────────────────────────
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {

        String refreshToken = request.refreshToken();

        // 토큰 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        // Redis에 저장된 RefreshToken과 비교
        String savedToken = redisTemplate.opsForValue().get("refresh:" + userId);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 유저 조회 후 새 AccessToken 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        String newAccessToken = jwtProvider.generateAccessToken(userId, user.getRole().name());

        return new TokenRefreshResponse(newAccessToken, 3600L);
    }

    // ── 로그아웃 ──────────────────────────────────────
    public void logout(Long userId) {
        // Redis에서 RefreshToken 삭제
        redisTemplate.delete("refresh:" + userId);
    }
}
