package com.bookhub.hyper_reserve.domain.concert.service;

import com.bookhub.hyper_reserve.domain.concert.dto.*;
import com.bookhub.hyper_reserve.domain.concert.repository.ConcertRepository;
import com.bookhub.hyper_reserve.domain.concert.repository.ConcertScheduleRepository;
import com.bookhub.hyper_reserve.domain.seat.repository.SeatRepository;
import com.bookhub.hyper_reserve.entity.Concert;
import com.bookhub.hyper_reserve.entity.ConcertSchedule;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @InjectMocks ConcertService concertService;

    @Mock ConcertRepository concertRepository;
    @Mock ConcertScheduleRepository scheduleRepository;
    @Mock SeatRepository seatRepository;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;

    // ── 공연 등록 테스트 ───────────────────────────────

    @Test
    @DisplayName("공연 등록 성공")
    void createConcert_success() {
        // given
        ConcertRequest request = new ConcertRequest(
                "2026 봄 콘서트",
                Concert.Category.CONCERT,
                "상세 설명",
                "올림픽 체조경기장",
                null,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 3),
                List.of(new ConcertRequest.ScheduleRequest(
                        LocalDate.of(2026, 4, 1), LocalTime.of(19, 0), 500
                )),
                List.of(new ConcertRequest.SeatGradeRequest(
                        com.bookhub.hyper_reserve.entity.Seat.Grade.VIP, 150000, 100
                ))
        );

        Concert savedConcert = Concert.builder()
                .title(request.title())
                .category(request.category())
                .venue(request.venue())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        given(concertRepository.save(any())).willReturn(savedConcert);
        given(scheduleRepository.save(any())).willReturn(
                ConcertSchedule.builder()
                        .concert(savedConcert)
                        .performDate(LocalDate.of(2026, 4, 1))
                        .performTime(LocalTime.of(19, 0))
                        .totalSeats(500)
                        .remainingSeats(500)
                        .build()
        );
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        ConcertCreateResponse response = concertService.createConcert(request);

        // then
        assertThat(response.title()).isEqualTo("2026 봄 콘서트");
        verify(concertRepository, times(1)).save(any());
        verify(scheduleRepository, times(1)).save(any());
        verify(seatRepository, times(1)).saveAll(any());
    }

    // ── 공연 상세 조회 테스트 ──────────────────────────

    @Test
    @DisplayName("공연 상세 조회 성공")
    void getConcert_success() {
        // given
        Long concertId = 1L;
        Concert concert = Concert.builder()
                .title("2026 봄 콘서트")
                .category(Concert.Category.CONCERT)
                .venue("올림픽 체조경기장")
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 3))
                .build();

        given(concertRepository.findById(concertId)).willReturn(Optional.of(concert));
        given(scheduleRepository.findByConcertId(concertId)).willReturn(List.of());

        // when
        ConcertDetailResponse response = concertService.getConcert(concertId);

        // then
        assertThat(response.title()).isEqualTo("2026 봄 콘서트");
        assertThat(response.venue()).isEqualTo("올림픽 체조경기장");
    }

    @Test
    @DisplayName("공연 상세 조회 실패 - 존재하지 않는 공연")
    void getConcert_fail_notFound() {
        // given
        given(concertRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> concertService.getConcert(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONCERT_NOT_FOUND);
    }

    @Test
    @DisplayName("공연 삭제 실패 - 존재하지 않는 공연")
    void deleteConcert_fail_notFound() {
        // given
        given(concertRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> concertService.deleteConcert(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONCERT_NOT_FOUND);
    }
}