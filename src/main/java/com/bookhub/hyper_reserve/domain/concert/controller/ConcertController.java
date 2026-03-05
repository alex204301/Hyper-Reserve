package com.bookhub.hyper_reserve.domain.concert.controller;

import com.bookhub.hyper_reserve.domain.concert.dto.*;
import com.bookhub.hyper_reserve.domain.concert.service.ConcertService;
import com.bookhub.hyper_reserve.entity.Concert;
import com.bookhub.hyper_reserve.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    // ── 공연 목록 조회 (일반) ──────────────────────────
    @GetMapping("/api/v1/concerts")
    public ResponseEntity<ApiResponse<Page<ConcertSummaryResponse>>> getConcerts(
            @RequestParam(required = false) Concert.Category category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ConcertSummaryResponse> response =
                concertService.getConcerts(category, keyword, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── 공연 상세 조회 (일반) ──────────────────────────
    @GetMapping("/api/v1/concerts/{concertId}")
    public ResponseEntity<ApiResponse<ConcertDetailResponse>> getConcert(
            @PathVariable Long concertId) {

        ConcertDetailResponse response = concertService.getConcert(concertId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── 어드민: 공연 등록 ──────────────────────────────
    @PostMapping("/api/v1/admin/concerts")
    public ResponseEntity<ApiResponse<ConcertCreateResponse>> createConcert(
            @Valid @RequestBody ConcertRequest request) {

        ConcertCreateResponse response = concertService.createConcert(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // ── 어드민: 공연 수정 ──────────────────────────────
    @PutMapping("/api/v1/admin/concerts/{concertId}")
    public ResponseEntity<ApiResponse<Void>> updateConcert(
            @PathVariable Long concertId,
            @Valid @RequestBody ConcertRequest request) {

        concertService.updateConcert(concertId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ── 어드민: 공연 삭제 ──────────────────────────────
    @DeleteMapping("/api/v1/admin/concerts/{concertId}")
    public ResponseEntity<ApiResponse<Void>> deleteConcert(
            @PathVariable Long concertId) {

        concertService.deleteConcert(concertId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
