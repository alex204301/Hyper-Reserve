package com.bookhub.hyper_reserve.domain.seat.repoistory;

import com.bookhub.hyper_reserve.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // 좌석 현황 조회: 특정 스케줄의 전체 좌석
    // 인덱스: (schedule_id, status) 활용
    List<Seat> findByScheduleId(Long scheduleId);

    // 예약 요청 시 좌석 조회 (비관적 락 - 동시성 제어)
    // SELECT ... FOR UPDATE: DB 레벨에서 해당 row를 잠금
    @Query("SELECT s FROM Seat s WHERE s.id = :seatId AND s.schedule.id = :scheduleId")
    Optional<Seat> findByIdAndScheduleId(
            @Param("seatId") Long seatId,
            @Param("scheduleId") Long scheduleId
    );
}
