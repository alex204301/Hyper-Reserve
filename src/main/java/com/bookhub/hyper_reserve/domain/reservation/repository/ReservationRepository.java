package com.bookhub.hyper_reserve.domain.reservation.repository;

import com.bookhub.hyper_reserve.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 내 예약 목록 조회
    // 인덱스: (user_id, status) 활용
    List<Reservation> findByUserId(Long userId);

    // 예약 상세 조회 시 연관 데이터 한 번에 fetch (N+1 방지)
    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.user
        JOIN FETCH r.seat
        JOIN FETCH r.schedule s
        JOIN FETCH s.concert
        WHERE r.id = :reservationId
    """)
    Optional<Reservation> findWithDetailsById(@Param("reservationId") Long reservationId);

    // 만료된 PENDING 예약 일괄 조회 (스케줄러에서 자동 취소 처리용)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.status = 'PENDING'
        AND r.expiredAt < :now
    """)
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    // 좌석 중복 예약 체크
    boolean existsBySeatIdAndStatusNot(Long seatId, Reservation.Status status);
}
