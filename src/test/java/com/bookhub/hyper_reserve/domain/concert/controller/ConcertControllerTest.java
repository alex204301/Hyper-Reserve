package com.bookhub.hyper_reserve.domain.concert.controller;

import com.bookhub.hyper_reserve.global.config.JwtProvider;
import com.bookhub.hyper_reserve.global.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookhub.hyper_reserve.domain.concert.dto.*;
import com.bookhub.hyper_reserve.domain.concert.service.ConcertService;
import com.bookhub.hyper_reserve.entity.Concert;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConcertController.class)
@Import(SecurityConfig.class)
class ConcertControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ConcertService concertService;
    @MockitoBean JwtProvider jwtProvider;

    @Test
    @DisplayName("공연 상세 조회 성공 - 200 반환")
    void getConcert_success() throws Exception {
        // given
        given(concertService.getConcert(1L)).willReturn(
                new ConcertDetailResponse(
                        1L, "2026 봄 콘서트", Concert.Category.CONCERT,
                        "설명", "올림픽 체조경기장",
                        LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3),
                        null, List.of()
                )
        );

        // when & then
        mockMvc.perform(get("/api/v1/concerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("2026 봄 콘서트"))
                .andExpect(jsonPath("$.data.venue").value("올림픽 체조경기장"));
    }

    @Test
    @DisplayName("공연 상세 조회 실패 - 존재하지 않는 공연 → 404")
    void getConcert_fail_notFound() throws Exception {
        // given
        given(concertService.getConcert(999L))
                .willThrow(new BusinessException(ErrorCode.CONCERT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/concerts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONCERT_NOT_FOUND"));
    }

    @Test
    @DisplayName("어드민 공연 등록 성공 - 201 반환")
    @WithMockUser(roles = "ADMIN")  // 어드민 권한으로 테스트
    void createConcert_success() throws Exception {
        // given
        ConcertRequest request = new ConcertRequest(
                "2026 봄 콘서트", Concert.Category.CONCERT, "설명",
                "올림픽 체조경기장", null,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3),
                List.of(new ConcertRequest.ScheduleRequest(
                        LocalDate.of(2026, 4, 1), LocalTime.of(19, 0), 500
                )),
                List.of(new ConcertRequest.SeatGradeRequest(
                        com.bookhub.hyper_reserve.entity.Seat.Grade.VIP, 150000, 100
                ))
        );
        given(concertService.createConcert(any()))
                .willReturn(new ConcertCreateResponse(1L, "2026 봄 콘서트"));

        // when & then
        mockMvc.perform(post("/api/v1/admin/concerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.concertId").value(1L))
                .andExpect(jsonPath("$.data.title").value("2026 봄 콘서트"));
    }

    @Test
    @DisplayName("어드민 공연 등록 실패 - 권한 없음 → 403")
    @WithMockUser(roles = "USER")  // 일반 유저로 시도
    void createConcert_fail_forbidden() throws Exception {
        // given
        ConcertRequest request = new ConcertRequest(
                "2026 봄 콘서트", Concert.Category.CONCERT, "설명",
                "올림픽 체조경기장", null,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3),
                List.of(new ConcertRequest.ScheduleRequest(
                        LocalDate.of(2026, 4, 1), LocalTime.of(19, 0), 500
                )),
                List.of(new ConcertRequest.SeatGradeRequest(
                        com.bookhub.hyper_reserve.entity.Seat.Grade.VIP, 150000, 100
                ))
        );

        // when & then
        mockMvc.perform(post("/api/v1/admin/concerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("어드민 공연 등록 실패 - 공연명 누락 → 400")
    @WithMockUser(roles = "ADMIN")
    void createConcert_fail_missingTitle() throws Exception {
        // given - title 없음
        String body = """
            {
                "category": "CONCERT",
                "venue": "올림픽 체조경기장",
                "startDate": "2026-04-01",
                "endDate": "2026-04-03",
                "schedules": [{"performDate":"2026-04-01","performTime":"19:00","totalSeats":500}],
                "seatGrades": [{"grade":"VIP","price":150000,"count":100}]
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/admin/concerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}