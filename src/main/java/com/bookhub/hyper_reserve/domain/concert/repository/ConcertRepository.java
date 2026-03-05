package com.bookhub.hyper_reserve.domain.concert.repository;

import com.bookhub.hyper_reserve.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

    // 목록 조회: 카테고리 + 날짜 범위 + 키워드 검색 (동적 쿼리)
    // 조건이 null이면 해당 필터를 무시하는 동적 쿼리
    // 인덱스: (category, start_date) 활용
    @Query("""
        SELECT c FROM Concert c
        WHERE (:category IS NULL OR c.category = :category)
        AND (:keyword IS NULL OR c.title LIKE %:keyword%)
        AND (:startDate IS NULL OR c.startDate >= :startDate)
        AND (:endDate IS NULL OR c.endDate <= :endDate)
    """)
    Page<Concert> searchConcerts(
            @Param("category") Concert.Category category,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}