package com.bookhub.hyper_reserve.entity;

import com.bookhub.hyper_reserve.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations",
        indexes = {
                // 내 예약 목록 조회 최적화
                @Index(name = "idx_reservations_user_id_status", columnList = "user_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ConcertSchedule schedule;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false, unique = true)  // ⭐ DB 레벨 중복 방지
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private int price;  // ⭐ 예약 당시 가격 스냅샷

    @Column(nullable = false)
    private LocalDateTime expiredAt;  // 결제 만료 시각 (default: 생성 후 10분)

    // 상태 변경 메서드
    public void confirm() {
        this.status = Status.CONFIRMED;
    }

    public void cancel() {
        if (this.status == Status.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }
        this.status = Status.CANCELLED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    public enum Status {
        PENDING,    // 결제 대기
        CONFIRMED,  // 예약 확정
        CANCELLED   // 취소
    }
}