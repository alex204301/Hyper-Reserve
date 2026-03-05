package com.bookhub.hyper_reserve.domain.payment.repository;

import com.bookhub.hyper_reserve.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 예약 ID로 결제 조회 (중복 결제 체크용)
    Optional<Payment> findByReservationId(Long reservationId);

    // 이미 결제된 예약인지 확인
    boolean existsByReservationId(Long reservationId);
}
