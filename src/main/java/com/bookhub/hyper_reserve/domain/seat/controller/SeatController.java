package com.bookhub.hyper_reserve.domain.seat.controller;

import com.bookhub.hyper_reserve.domain.seat.dto.SeatDetailResponse;
import com.bookhub.hyper_reserve.domain.seat.dto.SeatSummaryResponse;
import com.bookhub.hyper_reserve.domain.seat.service.SeatService;
import com.bookhub.hyper_reserve.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/concerts/{concertId}/schedules/{scheduleId}")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // 좌석 목록 조회 - row, number, status만 반환
    @GetMapping("/seats")
    public ResponseEntity<ApiResponse<List<SeatSummaryResponse>>> getSeats(
            @PathVariable Long concertId,
            @PathVariable Long scheduleId) {

        List<SeatSummaryResponse> response =
                seatService.getSeats(concertId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 좌석 상세 조회 - AVAILABLE인 좌석만 grade, price 등 전체 정보 반환
    @GetMapping("/seats/{seatId}")
    public ResponseEntity<ApiResponse<SeatDetailResponse>> getSeat(
            @PathVariable Long concertId,
            @PathVariable Long scheduleId,
            @PathVariable Long seatId) {

        SeatDetailResponse response =
                seatService.getSeat(concertId, scheduleId, seatId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
