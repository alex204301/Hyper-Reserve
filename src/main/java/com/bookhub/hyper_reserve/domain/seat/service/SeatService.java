package com.bookhub.hyper_reserve.domain.seat.service;

import com.bookhub.hyper_reserve.domain.concert.repository.ConcertScheduleRepository;
import com.bookhub.hyper_reserve.domain.seat.dto.SeatDetailResponse;
import com.bookhub.hyper_reserve.domain.seat.dto.SeatSummaryResponse;
import com.bookhub.hyper_reserve.domain.seat.repository.SeatRepository;
import com.bookhub.hyper_reserve.entity.ConcertSchedule;
import com.bookhub.hyper_reserve.entity.Seat;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ConcertScheduleRepository scheduleRepository;

    // ── 좌석 목록 조회 ─────────────────────────────────
    // 스케줄 존재 여부 검증 후 해당 스케줄의 전체 좌석을 간략하게 반환
    @Transactional(readOnly = true)
    public List<SeatSummaryResponse> getSeats(Long concertId, Long scheduleId) {

        // 스케줄 존재 여부 + 해당 공연 소속인지 검증
        ConcertSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getConcert().getId().equals(concertId)) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        return seatRepository.findByScheduleId(scheduleId).stream()
                .map(seat -> new SeatSummaryResponse(
                        seat.getId(),
                        seat.getSeat_row(),
                        seat.getNumber(),
                        seat.getStatus()
                ))
                .toList();
    }

    // ── 좌석 상세 조회 ─────────────────────────────────
    // AVAILABLE인 좌석만 상세 조회 허용
    // 이미 선점/확정된 좌석 정보를 외부에 노출할 필요가 없음
    @Transactional(readOnly = true)
    public SeatDetailResponse getSeat(Long concertId, Long scheduleId, Long seatId) {

        // 스케줄 존재 여부 + 해당 공연 소속인지 검증
        ConcertSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getConcert().getId().equals(concertId)) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        // 좌석 존재 여부 + 해당 스케줄 소속인지 검증
        Seat seat = seatRepository.findByIdAndScheduleId(seatId, scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        // AVAILABLE이 아닌 좌석은 상세 조회 불가
        if (seat.getStatus() != Seat.Status.AVAILABLE) {
            throw new BusinessException(ErrorCode.SEAT_ALREADY_TAKEN);
        }

        return new SeatDetailResponse(
                seat.getId(),
                scheduleId,
                seat.getSeat_row(),
                seat.getNumber(),
                seat.getGrade(),
                seat.getPrice(),
                seat.getStatus()
        );
    }
}
