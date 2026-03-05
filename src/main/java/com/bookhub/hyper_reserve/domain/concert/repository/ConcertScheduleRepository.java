package com.bookhub.hyper_reserve.domain.concert.repository;

import com.bookhub.hyper_reserve.entity.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {

    // 공연 상세 조회 시 해당 공연의 전체 회차 목록
    List<ConcertSchedule> findByConcertId(Long concertId);

    // 공연 삭제 시 해당 공연의 전체 스케줄 삭제
    // Seat 삭제 후에 호출해야 FK 제약 위반이 없음
    void deleteByConcertId(Long concertId);
}
