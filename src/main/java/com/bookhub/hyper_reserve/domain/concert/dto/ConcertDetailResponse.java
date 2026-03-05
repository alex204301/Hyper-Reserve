package com.bookhub.hyper_reserve.domain.concert.dto;

import com.bookhub.hyper_reserve.entity.Concert;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 공연 상세 응답
public record ConcertDetailResponse(
        Long concertId,
        String title,
        Concert.Category category,
        String description,
        String venue,
        LocalDate startDate,
        LocalDate endDate,
        String posterUrl,
        List<ScheduleResponse> schedules
) {
    public record ScheduleResponse(
            Long scheduleId,
            LocalDate performDate,
            LocalTime performTime,
            int remainingSeats
    ) {}
}
