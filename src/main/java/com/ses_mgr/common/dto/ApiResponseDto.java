package com.ses_mgr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private T data;
    private ErrorDto error;
    private String message;
    private boolean success;
    
    public ApiResponseDto(T data, String message) {
        this.data = data;
        this.message = message;
        this.success = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDto {
        private String code;
        private String message;
        private List<ErrorDetailDto> details;

        public void addDetail(String field, String message) {
            if (details == null) {
                details = new ArrayList<>();
            }
            details.add(new ErrorDetailDto(field, message));
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetailDto {
        private String field;
        private String message;
    }

    // ファクトリーメソッド - 成功レスポンス
    public static <T> ApiResponseDto<T> success(T data) {
        return ApiResponseDto.<T>builder()
                .data(data)
                .build();
    }

    // ファクトリーメソッド - エラーレスポンス
    public static <T> ApiResponseDto<T> error(String code, String message) {
        ErrorDto error = ErrorDto.builder()
                .code(code)
                .message(message)
                .build();

        return ApiResponseDto.<T>builder()
                .error(error)
                .build();
    }

    // ファクトリーメソッド - 詳細エラーレスポンス
    public static <T> ApiResponseDto<T> error(String code, String message, List<ErrorDetailDto> details) {
        ErrorDto error = ErrorDto.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();

        return ApiResponseDto.<T>builder()
                .error(error)
                .build();
    }
}