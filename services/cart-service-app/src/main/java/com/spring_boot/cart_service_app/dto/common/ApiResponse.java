package com.spring_boot.cart_service_app.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private T data;

    @JsonProperty("trace_id")
    private String traceId;

    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data, String message, String traceId) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, String traceId) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}