package jp.co.example.sesapp.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jp.co.example.sesapp.common.exception.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全てのAPIエンドポイントで発生した例外をグローバルに処理するためのコントローラーアドバイス。
 * RFC 7807（Problem Detail for HTTP APIs）規格に準拠したエラーレスポンスを生成します。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 認証例外ハンドラー
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("認証例外が発生しました: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("認証エラー");
        problem.setType(URI.create("https://example.com/errors/authentication"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("error_code", "AUTHENTICATION_FAILED");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    /**
     * アクセス拒否例外ハンドラー
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("アクセス拒否例外が発生しました: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("アクセス拒否");
        problem.setType(URI.create("https://example.com/errors/forbidden"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("error_code", "ACCESS_DENIED");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    /**
     * リソース未検出例外ハンドラー
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.info("リソース未検出例外が発生しました: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("リソースが見つかりません");
        problem.setType(URI.create("https://example.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("error_code", "RESOURCE_NOT_FOUND");
        
        if (ex.getResourceType() != null) {
            problem.setProperty("resource_type", ex.getResourceType());
        }
        if (ex.getResourceId() != null) {
            problem.setProperty("resource_id", ex.getResourceId());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * バリデーション例外ハンドラー
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ProblemDetail> handleValidationException(Exception ex, WebRequest request) {
        log.info("バリデーション例外が発生しました: {}", ex.getMessage());
        
        BindingResult bindingResult = null;
        if (ex instanceof MethodArgumentNotValidException manvEx) {
            bindingResult = manvEx.getBindingResult();
        } else if (ex instanceof BindException bindEx) {
            bindingResult = bindEx.getBindingResult();
        }
        
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (bindingResult != null) {
            fieldErrors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (error1, error2) -> error1 + ", " + error2
                    ));
        }
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "入力値の検証に失敗しました"
        );
        problem.setTitle("バリデーションエラー");
        problem.setType(URI.create("https://example.com/errors/validation"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("error_code", "VALIDATION_ERROR");
        problem.setProperty("errors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * リクエストパラメータ不足例外ハンドラー
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.info("リクエストパラメータ不足例外が発生しました: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                String.format("必須パラメータ '%s' (%s) が見つかりません", ex.getParameterName(), ex.getParameterType())
        );
        problem.setTitle("リクエストパラメータエラー");
        problem.setType(URI.create("https://example.com/errors/bad-request"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("error_code", "MISSING_PARAMETER");
        problem.setProperty("parameter_name", ex.getParameterName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * メソッド引数型不一致例外ハンドラー
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.info("メソッド引数型不一致例外が発生しました: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                String.format("パラメータ '%s' の値 '%s' を %s 型に変換できません",
                        ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown")
        );
        problem.setTitle("パラメータ型変換エラー");
        problem.setType(URI.create("https://example.com/errors/bad-request"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("error_code", "TYPE_MISMATCH");
        problem.setProperty("parameter_name", ex.getName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * 業務例外ハンドラー
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex, WebRequest request) {
        log.info("業務例外が発生しました: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("業務エラー");
        problem.setType(URI.create("https://example.com/errors/business"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("error_code", ex.getErrorCode());
        
        if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
            problem.setProperty("details", ex.getDetails());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * 汎用例外ハンドラー
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("予期しない例外が発生しました", ex);
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "サーバー内部でエラーが発生しました。管理者にお問い合わせください。"
        );
        problem.setTitle("サーバーエラー");
        problem.setType(URI.create("https://example.com/errors/server-error"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("error_code", "INTERNAL_SERVER_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}