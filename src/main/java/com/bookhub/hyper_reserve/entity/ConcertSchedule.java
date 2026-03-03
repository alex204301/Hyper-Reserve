package com.bookhub.hyper_reserve.entity;

import com.bookhub.hyper_reserve.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concert_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ConcertSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY: concert 정보가 필요할 때만 쿼리
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Column(nullable = false)
    private LocalDate performDate;

    @Column(nullable = false)
    private LocalTime performTime;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int remainingSeats;  // ⭐ Redis 캐싱 대상

    // 잔여 좌석 차감 메서드 (도메인 로직을 Entity에 포함)
    public void decreaseRemainingSeats() {
        if (this.remainingSeats <= 0) {
            throw new IllegalStateException("잔여 좌석이 없습니다.");
        }
        this.remainingSeats--;
    }

    // 잔여 좌석 복구 메서드 (예약 취소 시)
    public void increaseRemainingSeats() {
        this.remainingSeats++;
    }
}