package com.ses_mgr.common.exception;

import com.ses_mgr.common.dto.ApiResponseDto;
import com.ses_mgr.common.dto.ApiResponseDto.ErrorDetailDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    // 認証エラー処理
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAuthenticationException(AuthenticationException ex) {
        ApiResponseDto<Void> response = ApiResponseDto.error("UNAUTHORIZED", "認証が必要です。");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 権限エラー処理
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponseDto<Void> response = ApiResponseDto.error("FORBIDDEN", "このリソースにアクセスする権限がありません。");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // バリデーションエラー処理（@Valid）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorDetailDto> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorDetailDto(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiResponseDto<Void> response = ApiResponseDto.error("VALIDATION_ERROR", "入力データにエラーがあります。", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // バリデーションエラー処理（Bean Validation）
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorDetailDto> details = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorDetailDto(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collect(Collectors.toList());

        ApiResponseDto<Void> response = ApiResponseDto.error("VALIDATION_ERROR", "入力データにエラーがあります。", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // バインドエラー処理
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBindException(BindException ex) {
        List<ErrorDetailDto> details = new ArrayList<>();
        for (FieldError error : ex.getFieldErrors()) {
            details.add(new ErrorDetailDto(error.getField(), error.getDefaultMessage()));
        }

        ApiResponseDto<Void> response = ApiResponseDto.error("VALIDATION_ERROR", "入力データにエラーがあります。", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // エンティティが見つからない場合のエラー処理
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
        ApiResponseDto<Void> response = ApiResponseDto.error("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // データ整合性違反エラー処理
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                "RESOURCE_ALREADY_EXISTS",
                "リソースが既に存在します。詳細: " + ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 不正な引数例外処理
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponseDto<Void> response = ApiResponseDto.error("INVALID_ARGUMENT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 不正な状態例外処理
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalStateException(IllegalStateException ex) {
        ApiResponseDto<Void> response = ApiResponseDto.error("INVALID_STATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // その他の例外処理
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleExceptions(Exception ex, WebRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                "INTERNAL_SERVER_ERROR",
                "内部サーバーエラーが発生しました。詳細: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}