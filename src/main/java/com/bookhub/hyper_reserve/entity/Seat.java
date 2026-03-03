package com.bookhub.hyper_reserve.entity;

import com.bookhub.hyper_reserve.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats",
        indexes = {
                // 특정 스케줄의 예약 가능 좌석 조회 최적화
                @Index(name = "idx_seats_schedule_id_status", columnList = "schedule_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ConcertSchedule schedule;

    @Column(nullable = false, length = 5)
    private String row;       // A, B, C ...

    @Column(nullable = false)
    private int number;       // 1, 2, 3 ...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.AVAILABLE;

    // ⭐ 상태 변경 메서드 (동시성 제어 핵심)
    public void reserve() {
        if (this.status != Status.AVAILABLE) {
            throw new IllegalStateException("이미 선점된 좌석입니다.");
        }
        this.status = Status.RESERVED;
    }

    public void confirm() {
        this.status = Status.TAKEN;
    }

    public void release() {
        this.status = Status.AVAILABLE;
    }

    public enum Grade {
        VIP, R, S
    }

    public enum Status {
        AVAILABLE,  // 예약 가능
        RESERVED,   // 임시 선점 (결제 대기)
        TAKEN       // 예약 확정
    }
}