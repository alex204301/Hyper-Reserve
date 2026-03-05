package com.bookhub.hyper_reserve.domain.auth.dto;

// 토큰 재발급 응답
public record TokenRefreshResponse(
        String accessToken,
        long expiresIn
) {}
