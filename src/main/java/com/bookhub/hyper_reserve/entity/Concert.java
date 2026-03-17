package com.bookhub.hyper_reserve.entity;

import com.bookhub.hyper_reserve.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concerts",
        indexes = {
                // 카테고리 필터 + 날짜 범위 검색 최적화
                @Index(name = "idx_concerts_category_start_date", columnList = "category, start_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Concert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 200)
    private String venue;

    @Column(length = 500)
    private String posterUrl;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    public enum Category {
        CONCERT, MUSICAL, EXHIBITION
    }
}