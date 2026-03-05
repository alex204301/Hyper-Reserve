package com.bookhub.hyper_reserve.domain.concert.repository;

import com.bookhub.hyper_reserve.entity.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {

    // 공연 상세 조회 시 해당 공연의 전체 회차 목록
    List<ConcertSchedule> findByConcertId(Long concertId);
}
