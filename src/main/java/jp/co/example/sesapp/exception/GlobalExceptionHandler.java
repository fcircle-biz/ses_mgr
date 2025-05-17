package jp.co.example.sesapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * アプリケーション全体の例外ハンドラー.
 * コントローラーから発生した例外を集中的に処理します。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * REST APIのエラーレスポンス用クラス.
     */
    public static class ApiError {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private Map<String, String> validationErrors;

        public ApiError(LocalDateTime timestamp, int status, String error, String message, 
                        String path, Map<String, String> validationErrors) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.validationErrors = validationErrors;
        }

        // Getterメソッド
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }

        public Map<String, String> getValidationErrors() {
            return validationErrors;
        }
    }

    /**
     * APIリクエスト処理中のBusinessExceptionを処理します.
     *
     * @param ex 発生したBusinessException
     * @param request Webリクエスト
     * @return エラー情報を含むResponseEntity
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, WebRequest request, 
                                                          HttpServletRequest httpRequest) {
        logger.warn("Business exception occurred: {}", ex.getMessage(), ex);
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Business Rule Violation",
                ex.getMessage(),
                httpRequest.getRequestURI(),
                null
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * APIリクエスト処理中のSystemExceptionを処理します.
     *
     * @param ex 発生したSystemException
     * @param request Webリクエスト
     * @return エラー情報を含むResponseEntity
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiError> handleSystemException(SystemException ex, WebRequest request, 
                                                        HttpServletRequest httpRequest) {
        logger.error("System exception occurred: {}", ex.getMessage(), ex);
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "System Error",
                "An internal system error occurred. Please try again later.",
                httpRequest.getRequestURI(),
                null
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * バリデーションエラーを処理します.
     *
     * @param ex 発生したMethodArgumentNotValidException
     * @param request Webリクエスト
     * @return エラー情報を含むResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, 
                                                             HttpServletRequest request) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? 
                                      fieldError.getDefaultMessage() : "Invalid value",
                        (error1, error2) -> error1 + ", " + error2
                ));
        
        logger.warn("Validation error occurred: {}", errors);
        
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "入力値が正しくありません。",
                request.getRequestURI(),
                errors
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * 認可エラーを処理します.
     *
     * @param ex 発生したAccessDeniedException
     * @param request Webリクエスト
     * @return エラー情報を含むModelAndView
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        
        if (isApiRequest(request)) {
            // API呼び出しの場合はJSON応答を返す
            return null; // Spring Securityが適切なレスポンスを返す
        } else {
            // Web画面の場合はエラーページにリダイレクト
            ModelAndView modelAndView = new ModelAndView("error/403");
            modelAndView.addObject("errorMessage", "アクセス権限がありません。");
            modelAndView.addObject("timestamp", LocalDateTime.now());
            return modelAndView;
        }
    }

    /**
     * その他の例外を処理します.
     *
     * @param ex 発生した例外
     * @param request Webリクエスト
     * @return エラー情報を含むModelAndViewまたはResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public Object handleAllExceptions(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        
        if (isApiRequest(request)) {
            // API呼び出しの場合はJSON応答を返す
            ApiError apiError = new ApiError(
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "予期しないエラーが発生しました。",
                    request.getRequestURI(),
                    null
            );
            return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // Web画面の場合はエラーページにリダイレクト
            ModelAndView modelAndView = new ModelAndView("error/500");
            modelAndView.addObject("errorMessage", "システムエラーが発生しました。");
            modelAndView.addObject("timestamp", LocalDateTime.now());
            return modelAndView;
        }
    }

    /**
     * リクエストがAPIコールかどうかを判定します.
     *
     * @param request HTTPリクエスト
     * @return APIリクエストの場合はtrue
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/");
    }
}