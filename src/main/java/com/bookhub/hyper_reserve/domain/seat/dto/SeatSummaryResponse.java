package com.bookhub.hyper_reserve.domain.seat.dto;

import com.bookhub.hyper_reserve.entity.Seat;

// 좌석 목록 응답 (간략) - row, number, status만 노출
public record SeatSummaryResponse(
        Long seatId,
        String seat_row,
        int number,
        Seat.Status status   // AVAILABLE / RESERVED / TAKEN
) {}
