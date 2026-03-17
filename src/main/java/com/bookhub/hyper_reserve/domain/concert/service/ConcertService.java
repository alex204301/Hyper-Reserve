package com.bookhub.hyper_reserve.domain.concert.service;

import com.bookhub.hyper_reserve.domain.concert.dto.*;
import com.bookhub.hyper_reserve.domain.concert.repository.ConcertRepository;
import com.bookhub.hyper_reserve.domain.concert.repository.ConcertScheduleRepository;
import com.bookhub.hyper_reserve.domain.seat.repository.SeatRepository;
import com.bookhub.hyper_reserve.entity.Concert;
import com.bookhub.hyper_reserve.entity.ConcertSchedule;
import com.bookhub.hyper_reserve.entity.Seat;
import com.bookhub.hyper_reserve.global.exception.BusinessException;
import com.bookhub.hyper_reserve.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // ── 공연 목록 조회 ─────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ConcertSummaryResponse> getConcerts(
            Concert.Category category,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        return concertRepository
                .searchConcerts(category, keyword, startDate, endDate, pageable)
                .map(concert -> {
                    List<ConcertSchedule> schedules =
                            scheduleRepository.findByConcertId(concert.getId());

                    // Redis에서 스케줄별 잔여 좌석 합산
                    // key: "remaining:{scheduleId}"
                    int remainingSeats = schedules.stream()
                            .mapToInt(s -> {
                                String cached = redisTemplate.opsForValue()
                                        .get("remaining:" + s.getId());
                                return cached != null ? Integer.parseInt(cached) : 0;
                            })
                            .sum();

                    // 최소 가격 계산 (첫 번째 스케줄 기준)
                    int minPrice = schedules.isEmpty() ? 0 :
                            seatRepository.findByScheduleId(schedules.get(0).getId())
                                    .stream()
                                    .mapToInt(Seat::getPrice)
                                    .min()
                                    .orElse(0);

                    return new ConcertSummaryResponse(
                            concert.getId(),
                            concert.getTitle(),
                            concert.getCategory(),
                            concert.getVenue(),
                            concert.getStartDate(),
                            concert.getEndDate(),
                            concert.getPosterUrl(),
                            minPrice,
                            remainingSeats
                    );
                });
    }

    // ── 공연 상세 조회 ─────────────────────────────────
    @Transactional(readOnly = true)
    public ConcertDetailResponse getConcert(Long concertId) {

        Concert concert = concertRepository.findByIdAndIsDeletedFalse(concertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND));

        List<ConcertSchedule> schedules =
                scheduleRepository.findByConcertId(concertId);

        List<ConcertDetailResponse.ScheduleResponse> scheduleResponses = schedules.stream()
                .map(s -> new ConcertDetailResponse.ScheduleResponse(
                        s.getId(),
                        s.getPerformDate(),
                        s.getPerformTime(),
                        s.getRemainingSeats()
                ))
                .toList();

        return new ConcertDetailResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getCategory(),
                concert.getDescription(),
                concert.getVenue(),
                concert.getStartDate(),
                concert.getEndDate(),
                concert.getPosterUrl(),
                scheduleResponses
        );
    }

    // ── 어드민: 공연 등록 ──────────────────────────────
    @Transactional
    public ConcertCreateResponse createConcert(ConcertRequest request) {

        // 1. 공연 저장
        Concert concert = Concert.builder()
                .title(request.title())
                .category(request.category())
                .description(request.description())
                .venue(request.venue())
                .posterUrl(request.posterUrl())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        Concert savedConcert = concertRepository.save(concert);

        // 2. 스케줄 + 좌석 생성
        for (ConcertRequest.ScheduleRequest scheduleReq : request.schedules()) {
            ConcertSchedule schedule = ConcertSchedule.builder()
                    .concert(savedConcert)
                    .performDate(scheduleReq.performDate())
                    .performTime(scheduleReq.performTime())
                    .totalSeats(scheduleReq.totalSeats())
                    .remainingSeats(scheduleReq.totalSeats())  // 초기엔 전체가 잔여석
                    .build();

            ConcertSchedule savedSchedule = scheduleRepository.save(schedule);

            // 3. 좌석 등급별 생성
            List<Seat> seats = new ArrayList<>();
            for (ConcertRequest.SeatGradeRequest gradeReq : request.seatGrades()) {
                for (int i = 1; i <= gradeReq.count(); i++) {
                    seats.add(Seat.builder()
                            .schedule(savedSchedule)
                            .seat_row(gradeReq.grade().name())  // VIP, R, S 를 열 이름으로 사용
                            .number(i)
                            .grade(gradeReq.grade())
                            .price(gradeReq.price())
                            .build());
                }
            }
            seatRepository.saveAll(seats);

            // 4. Redis에 스케줄별 잔여 좌석 캐싱
            // key: "remaining:{scheduleId}"
            redisTemplate.opsForValue().set(
                    "remaining:" + savedSchedule.getId(),
                    String.valueOf(scheduleReq.totalSeats())
            );
        }

        return new ConcertCreateResponse(savedConcert.getId(), savedConcert.getTitle());
    }

    // ── 어드민: 공연 수정 ──────────────────────────────
    @Transactional
    public void updateConcert(Long concertId, ConcertRequest request) {

        // 공연 존재 여부 확인
        concertRepository.findByIdAndIsDeletedFalse(concertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND));

        // 기존 공연 정보 수정 (스케줄/좌석은 불가)
        concertRepository.updateConcertInfo(
                concertId,
                request.title(),
                request.category(),
                request.description(),
                request.venue(),
                request.posterUrl(),
                request.startDate(),
                request.endDate()
        );
    }

    // ── 어드민: 공연 삭제 ──────────────────────────────
    @Transactional
    public void deleteConcert(Long concertId) {

        concertRepository.findById(concertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND));

        // 1. 스케줄 조회 (DB 삭제 전에 먼저)
        List<ConcertSchedule> schedules =
                scheduleRepository.findByConcertId(concertId);

        // 2. Redis 캐시 삭제
        schedules.forEach(s -> redisTemplate.delete("remaining:" + s.getId()));

        // 3. 자식부터 순서대로 삭제 (FK 제약 위반 방지) -> 이전 방식
        // schedules.forEach(s -> seatRepository.deleteByScheduleId(s.getId()));
        // scheduleRepository.deleteByConcertId(concertId);

        // 3. soft delete 방식으로 실제로 삭제하지는 않고 필드값을 바꿔서 표시
        concertRepository.updateIsDeletedById(concertId);
    }
}
