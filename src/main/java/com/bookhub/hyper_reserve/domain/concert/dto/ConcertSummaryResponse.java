package com.bookhub.hyper_reserve.domain.concert.dto;

import com.bookhub.hyper_reserve.entity.Concert;
import java.time.LocalDate;

// 공연 목록 응답 (간략)
public record ConcertSummaryResponse(
        Long concertId,
        String title,
        Concert.Category category,
        String venue,
        LocalDate startDate,
        LocalDate endDate,
        String posterUrl,
        int minPrice,           // 가장 저렴한 좌석 가격
        int remainingSeats      // Redis 캐싱 값
) {}
