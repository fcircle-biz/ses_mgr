package jp.co.example.sesapp.common.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;
import jp.co.example.sesapp.common.auth.service.PasswordResetService;
import jp.co.example.sesapp.common.exception.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * パスワードリセット機能のコントローラー
 */
@RestController
@RequestMapping("/api/auth/password")
@Tag(name = "パスワードリセットAPI", description = "パスワードリセット機能を提供するAPI")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * パスワードリセットリクエスト
     */
    @PostMapping("/reset-request")
    @Operation(summary = "パスワードリセット要求", description = "パスワードリセットのリクエストを送信し、リセット用のメールを送信します。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "パスワードリセット要求成功"),
            @ApiResponse(responseCode = "400", description = "入力エラー",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {

        // パスワードリセットトークンを作成
        PasswordResetToken token = passwordResetService.createPasswordResetToken(request.getEmail());

        // メール送信
        passwordResetService.sendPasswordResetEmail(token, request.getEmail());

        // レスポンス作成
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "パスワードリセット用のメールを送信しました。メールに記載されている手順に従ってください。");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * パスワードリセット実行
     */
    @PostMapping("/reset")
    @Operation(summary = "パスワードリセット実行", description = "パスワードリセットトークンを使用して、新しいパスワードを設定します。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "パスワードリセット成功"),
            @ApiResponse(responseCode = "400", description = "入力エラーまたは無効なトークン",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody PasswordResetExecution request) {

        // パスワードリセットを実行
        passwordResetService.resetPassword(
                request.getToken(),
                request.getEmail(),
                request.getPassword()
        );

        // レスポンス作成
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "パスワードが正常にリセットされました。新しいパスワードでログインしてください。");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * パスワードリセットリクエストDTO
     */
    public static class PasswordResetRequest {
        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "有効なメールアドレスを入力してください")
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * パスワードリセット実行DTO
     */
    public static class PasswordResetExecution {
        @NotBlank(message = "トークンは必須です")
        private String token;

        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "有効なメールアドレスを入力してください")
        private String email;

        @NotBlank(message = "パスワードは必須です")
        @Size(min = 8, message = "パスワードは8文字以上である必要があります")
        private String password;

        @NotBlank(message = "パスワード確認は必須です")
        private String passwordConfirmation;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPasswordConfirmation() {
            return passwordConfirmation;
        }

        public void setPasswordConfirmation(String passwordConfirmation) {
            this.passwordConfirmation = passwordConfirmation;
        }
    }
}