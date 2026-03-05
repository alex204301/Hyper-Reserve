package com.bookhub.hyper_reserve.domain.auth.dto;

// 로그인 응답
public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn  // 초 단위
) {}
