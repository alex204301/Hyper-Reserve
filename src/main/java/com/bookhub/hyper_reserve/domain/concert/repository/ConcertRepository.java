package com.bookhub.hyper_reserve.domain.concert.repository;

import com.bookhub.hyper_reserve.entity.Concert;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

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
        AND (c.isDeleted = false)
    """)
    Page<Concert> searchConcerts(
            @Param("category") Concert.Category category,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Soft Delete 방식
    @Modifying
    @Query("UPDATE Concert c SET c.isDeleted = true WHERE c.id = :id")
    void updateIsDeletedById(@Param("id") Long concertId);

    // 공연 조회 시 실제 존재하는지, 삭제되지 않았는지 확인
    Optional<Concert> findByIdAndIsDeletedFalse(Long Id);

    // 공연 정보 수정 쿼리
    @Modifying
    @Query("""
        UPDATE Concert c
        SET c.title = :title,
            c.category = :category,
            c.description = :description,
            c.venue = :venue,
            c.posterUrl = :posterUrl,
            c.startDate = :startDate,
            c.endDate = :endDate
        WHERE (c.id = :id)
    """)
    void updateConcertInfo(
            @Param("id") Long concertId,
            @Param("title") String title,
            @Param("category") Concert.Category category,
            @Param("description") String description,
            @Param("venue") String venue,
            @Param("posterUrl") String posterUrl,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}