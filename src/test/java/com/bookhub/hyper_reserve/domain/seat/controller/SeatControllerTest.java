package com.bookhub.hyper_reserve.domain.seat.controller;

import com.bookhub.hyper_reserve.domain.seat.dto.SeatDetailResponse;
import com.bookhub.hyper_reserve.domain.seat.dto.SeatSummaryResponse;
import com.bookhub.hyper_reserve.domain.seat.service.SeatService;
import com.bookhub.hyper_reserve.entity.Seat;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
class SeatControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean SeatService seatService;

    @Test
    @DisplayName("좌석 목록 조회 성공 - 200 반환")
    void getSeats_success() throws Exception {
        // given
        given(seatService.getSeats(1L, 10L)).willReturn(List.of(
                new SeatSummaryResponse(101L, "A", 1, Seat.Status.AVAILABLE),
                new SeatSummaryResponse(102L, "A", 2, Seat.Status.TAKEN)
        ));

        // when & then
        mockMvc.perform(get("/api/v1/concerts/1/schedules/10/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].seatId").value(101))
                .andExpect(jsonPath("$.data[0].seat_row").value("A"))
                .andExpect(jsonPath("$.data[0].number").value(1))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data[1].status").value("TAKEN"));
    }

    @Test
    @DisplayName("좌석 목록 조회 실패 - 존재하지 않는 스케줄 → 404")
    void getSeats_fail_scheduleNotFound() throws Exception {
        // given
        given(seatService.getSeats(1L, 999L))
                .willThrow(new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/concerts/1/schedules/999/seats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
    }

    @Test
    @DisplayName("좌석 상세 조회 성공 - 200 반환")
    void getSeat_success() throws Exception {
        // given
        given(seatService.getSeat(1L, 10L, 101L)).willReturn(
                new SeatDetailResponse(101L, 10L, "A", 1, Seat.Grade.VIP, 150000, Seat.Status.AVAILABLE)
        );

        // when & then
        mockMvc.perform(get("/api/v1/concerts/1/schedules/10/seats/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.seat_row").value("A"))
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.seat.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.grade").value("VIP"))
                .andExpect(jsonPath("$.data.price").value(150000));
    }

    @Test
    @DisplayName("좌석 상세 조회 실패 - 이미 선점된 좌석 → 409")
    void getSeat_fail_alreadyTaken() throws Exception {
        // given
        given(seatService.getSeat(1L, 10L, 101L))
                .willThrow(new BusinessException(ErrorCode.SEAT_ALREADY_TAKEN));

        // when & then
        mockMvc.perform(get("/api/v1/concerts/1/schedules/10/seats/101"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SEAT_ALREADY_TAKEN"));
    }
}