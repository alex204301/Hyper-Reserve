package com.bookhub.hyper_reserve.domain.concert.dto;

import com.bookhub.hyper_reserve.entity.Concert;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 공연 등록/수정 요청
public record ConcertRequest(

        @NotBlank(message = "공연명은 필수입니다.")
        String title,

        @NotNull(message = "카테고리는 필수입니다.")
        Concert.Category category,

        String description,

        @NotBlank(message = "공연장은 필수입니다.")
        String venue,

        String posterUrl,

        @NotNull(message = "공연 시작일은 필수입니다.")
        LocalDate startDate,

        @NotNull(message = "공연 종료일은 필수입니다.")
        LocalDate endDate,

        @NotEmpty(message = "스케줄은 최소 1개 이상이어야 합니다.")
        @Valid
        List<ScheduleRequest> schedules,

        @NotEmpty(message = "좌석 등급은 최소 1개 이상이어야 합니다.")
        @Valid
        List<SeatGradeRequest> seatGrades
) {
    // 스케줄 요청 (중첩 record)
    public record ScheduleRequest(

            @NotNull(message = "공연 날짜는 필수입니다.")
            LocalDate performDate,

            @NotNull(message = "공연 시간은 필수입니다.")
            LocalTime performTime,

            @Min(value = 1, message = "총 좌석 수는 1 이상이어야 합니다.")
            int totalSeats
    ) {}

    // 좌석 등급 요청 (중첩 record)
    public record SeatGradeRequest(

            @NotNull(message = "좌석 등급은 필수입니다.")
            com.bookhub.hyper_reserve.entity.Seat.Grade grade,

            @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
            int price,

            @Min(value = 1, message = "좌석 수는 1 이상이어야 합니다.")
            int count
    ) {}
}
