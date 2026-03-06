package com.bookhub.hyper_reserve.domain.seat.service;

import com.bookhub.hyper_reserve.domain.concert.repository.ConcertScheduleRepository;
import com.bookhub.hyper_reserve.domain.seat.dto.SeatDetailResponse;
import com.bookhub.hyper_reserve.domain.seat.dto.SeatSummaryResponse;
import com.bookhub.hyper_reserve.domain.seat.repository.SeatRepository;
import com.bookhub.hyper_reserve.entity.Concert;
import com.bookhub.hyper_reserve.entity.ConcertSchedule;
import com.bookhub.hyper_reserve.entity.Seat;
import com.bookhub.hyper_reserve.global.config.JwtProvider;
import com.bookhub.hyper_reserve.global.config.SecurityConfig;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Import(SecurityConfig.class)
@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @InjectMocks SeatService seatService;
    @Mock SeatRepository seatRepository;
    @Mock ConcertScheduleRepository scheduleRepository;
    @MockitoBean JwtProvider jwtProvider;

    // 테스트용 공통 픽스처
    private Concert makeConcert(Long concertId) {
        return Concert.builder()
                .id(concertId)
                .title("테스트 콘서트")
                .category(Concert.Category.CONCERT)
                .venue("올림픽 체조경기장")
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 1))
                .build();
    }

    private ConcertSchedule makeSchedule(Long scheduleId, Concert concert) {
        return ConcertSchedule.builder()
                .id(scheduleId)
                .concert(concert)
                .performDate(LocalDate.of(2026, 4, 1))
                .performTime(LocalTime.of(19, 0))
                .totalSeats(100)
                .remainingSeats(100)
                .build();
    }

    // ── 좌석 목록 조회 테스트 ──────────────────────────

    @Test
    @DisplayName("좌석 목록 조회 성공")
    void getSeats_success() {
        // given
        Long concertId = 1L;
        Long scheduleId = 10L;

        Concert concert = makeConcert(concertId);
        ConcertSchedule schedule = makeSchedule(scheduleId, concert);

        Seat seat1 = Seat.builder()
                .schedule(schedule).seat_row("A").number(1)
                .grade(Seat.Grade.VIP).price(150000).build();
        Seat seat2 = Seat.builder()
                .schedule(schedule).seat_row("A").number(2)
                .grade(Seat.Grade.VIP).price(150000).build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(seatRepository.findByScheduleId(scheduleId)).willReturn(List.of(seat1, seat2));

        // when
        List<SeatSummaryResponse> result = seatService.getSeats(concertId, scheduleId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().seat_row()).isEqualTo("A");
        assertThat(result.getFirst().status()).isEqualTo(Seat.Status.AVAILABLE);
        assertThat(result.getFirst().number()).isEqualTo(1);
    }

    @Test
    @DisplayName("좌석 목록 조회 실패 - 존재하지 않는 스케줄")
    void getSeats_fail_scheduleNotFound() {
        // given
        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> seatService.getSeats(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCHEDULE_NOT_FOUND);
    }

    // ── 좌석 상세 조회 테스트 ──────────────────────────

    @Test
    @DisplayName("좌석 상세 조회 성공 - AVAILABLE 좌석")
    void getSeat_success() {
        // given
        Long concertId = 1L;
        Long scheduleId = 10L;
        Long seatId = 101L;

        Concert concert = makeConcert(concertId);
        ConcertSchedule schedule = makeSchedule(scheduleId, concert);
        Seat seat = Seat.builder()
                .schedule(schedule).seat_row("A").number(1)
                .grade(Seat.Grade.VIP).price(150000).build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(seatRepository.findByIdAndScheduleId(seatId, scheduleId))
                .willReturn(Optional.of(seat));

        // when
        SeatDetailResponse result = seatService.getSeat(concertId, scheduleId, seatId);

        // then
        assertThat(result.seat_row()).isEqualTo("A");
        assertThat(result.grade()).isEqualTo(Seat.Grade.VIP);
        assertThat(result.price()).isEqualTo(150000);
        assertThat(result.number()).isEqualTo(1);
    }

    @Test
    @DisplayName("좌석 상세 조회 실패 - 이미 선점된 좌석")
    void getSeat_fail_alreadyTaken() {
        // given
        Long concertId = 1L;
        Long scheduleId = 10L;
        Long seatId = 101L;

        Concert concert = makeConcert(concertId);
        ConcertSchedule schedule = makeSchedule(scheduleId, concert);
        Seat reservedSeat = Seat.builder()
                .schedule(schedule).seat_row("A").number(1)
                .grade(Seat.Grade.VIP).price(150000).build();
        reservedSeat.reserve();  // AVAILABLE → RESERVED 상태로 변경

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(seatRepository.findByIdAndScheduleId(seatId, scheduleId))
                .willReturn(Optional.of(reservedSeat));

        // when & then
        assertThatThrownBy(() -> seatService.getSeat(concertId, scheduleId, seatId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_ALREADY_TAKEN);
    }

    @Test
    @DisplayName("좌석 상세 조회 실패 - 존재하지 않는 좌석")
    void getSeat_fail_seatNotFound() {
        // given
        Long concertId = 1L;
        Long scheduleId = 10L;

        Concert concert = makeConcert(concertId);
        ConcertSchedule schedule = makeSchedule(scheduleId, concert);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(seatRepository.findByIdAndScheduleId(999L, scheduleId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> seatService.getSeat(concertId, scheduleId, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_NOT_FOUND);
    }
}