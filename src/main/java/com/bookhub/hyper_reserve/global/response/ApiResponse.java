package com.bookhub.hyper_reserve.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드는 JSON에서 제외
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String code;
    private final LocalDateTime timestamp;

    // 성공 응답 (데이터 있음)
    private ApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.message = "요청이 성공했습니다.";
        this.code = null;
        this.timestamp = LocalDateTime.now();
    }

    // 성공 응답 (데이터 없음)
    private ApiResponse() {
        this.success = true;
        this.data = null;
        this.message = "요청이 성공했습니다.";
        this.code = null;
        this.timestamp = LocalDateTime.now();
    }

    // 실패 응답
    private ApiResponse(String code, String message) {
        this.success = false;
        this.data = null;
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message);
    }
}
