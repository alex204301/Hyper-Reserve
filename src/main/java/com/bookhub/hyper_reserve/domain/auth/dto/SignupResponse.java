package com.bookhub.hyper_reserve.domain.auth.dto;

// 회원가입 응답
public record SignupResponse(
        Long userId,
        String email,
        String name
) {}
