package com.bookhub.hyper_reserve.domain.concert.dto;

// 공연 등록/수정 응답
public record ConcertCreateResponse(
        Long concertId,
        String title
) {}
