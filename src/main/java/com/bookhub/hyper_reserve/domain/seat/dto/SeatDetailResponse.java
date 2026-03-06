package com.bookhub.hyper_reserve.domain.seat.dto;

import com.bookhub.hyper_reserve.entity.Seat;

// 좌석 상세 응답 - grade, price 등 전체 정보 노출
public record SeatDetailResponse(
        Long seatId,
        Long scheduleId,
        String seat_row,
        int number,
        Seat.Grade grade,    // VIP / R / S
        int price,
        Seat.Status status
) {}
