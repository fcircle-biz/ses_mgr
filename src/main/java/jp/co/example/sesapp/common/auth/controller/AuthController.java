package jp.co.example.sesapp.common.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.co.example.sesapp.common.auth.domain.dto.*;
import jp.co.example.sesapp.common.auth.service.AuthenticationService;
import jp.co.example.sesapp.common.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 認証関連のエンドポイントを提供するコントローラー
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "認証API", description = "認証・認可に関する操作を提供するAPI")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * ログイン認証を行うエンドポイント
     *
     * @param credentials 認証情報
     * @param request HTTPリクエスト
     * @return JWT認証トークンを含むレスポンス
     */
    @PostMapping("/login")
    @Operation(summary = "ユーザーログイン", description = "ユーザーのログイン認証を行い、認証トークンを発行します。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ログイン成功"),
        @ApiResponse(responseCode = "401", description = "認証失敗", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> authenticate(
            @Valid @RequestBody Credentials credentials,
            HttpServletRequest request) {

        String deviceId = request.getHeader("X-Device-ID");
        String deviceInfo = request.getHeader("User-Agent");
        String ipAddress = getClientIp(request);

        AuthenticationResponse authResponse = authenticationService.authenticate(
                credentials, deviceId, deviceInfo, ipAddress);

        // レスポンスをOpenAPI仕様に合わせてフォーマット
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        data.put("access_token", authResponse.getAccessToken());
        data.put("refresh_token", authResponse.getRefreshToken());
        data.put("token_type", authResponse.getTokenType());
        data.put("expires_in", authResponse.getExpiresIn());
        
        if (authResponse.getUser() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", authResponse.getUser().getId());
            userInfo.put("email", authResponse.getUser().getEmail());
            userInfo.put("name", authResponse.getUser().getName());
            userInfo.put("role", authResponse.getUser().getRole());
            data.put("user", userInfo);
        }
        
        response.put("data", data);
        
        if (authResponse.isRequiresMfa()) {
            response.put("requires_mfa", true);
            if (authResponse.getMfaChallenge() != null) {
                response.put("mfa_challenge", authResponse.getMfaChallenge());
            }
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * ログアウトするエンドポイント
     */
    @PostMapping("/logout")
    @Operation(summary = "ユーザーログアウト", description = "現在のセッションをログアウトし、発行されたトークンを無効化します。")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ログアウト成功"),
        @ApiResponse(responseCode = "401", description = "認証エラー", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        authenticationService.logout(logoutRequest.getRefreshToken());
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "ログアウトが完了しました。");
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * リフレッシュトークンを使用して新しいJWTトークンを取得するエンドポイント
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "アクセストークンの更新", description = "有効期限切れのアクセストークンを、リフレッシュトークンを使用して再発行します。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "トークン更新成功"),
        @ApiResponse(responseCode = "401", description = "無効なリフレッシュトークン", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest tokenRefreshRequest,
            HttpServletRequest request) {

        String deviceId = request.getHeader("X-Device-ID");
        String deviceInfo = request.getHeader("User-Agent");
        String ipAddress = getClientIp(request);

        AuthenticationResponse authResponse = authenticationService.refreshToken(
                tokenRefreshRequest.getRefreshToken(), deviceId, deviceInfo, ipAddress);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        data.put("access_token", authResponse.getAccessToken());
        data.put("token_type", authResponse.getTokenType());
        data.put("expires_in", authResponse.getExpiresIn());
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * ユーザープロフィール情報を取得するエンドポイント
     */
    @GetMapping("/profile")
    @Operation(summary = "ユーザープロフィール取得", description = "ログインユーザーの詳細プロフィール情報を取得します。")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "プロフィール取得成功"),
        @ApiResponse(responseCode = "401", description = "認証エラー", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        // 現在の認証ユーザーのプロフィール情報を取得
        UserDto user = authenticationService.getCurrentUserProfile();
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        data.put("id", user.getId());
        data.put("email", user.getEmail());
        data.put("name", user.getName());
        data.put("department", user.getDepartment());
        data.put("position", user.getPosition());
        data.put("phone", user.getPhone());
        data.put("role", user.getRole());
        data.put("last_login_at", user.getLastLoginAt());
        data.put("created_at", user.getCreatedAt());
        data.put("updated_at", user.getUpdatedAt());
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * ユーザープロフィール情報を更新するエンドポイント
     */
    @PutMapping("/profile")
    @Operation(summary = "ユーザープロフィール更新", description = "ログインユーザーのプロフィール情報を更新します。")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "プロフィール更新成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "認証エラー", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Valid @RequestBody UserUpdateDto updateDto) {
        
        UserDto updatedUser = authenticationService.updateUserProfile(updateDto);
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        data.put("id", updatedUser.getId());
        data.put("email", updatedUser.getEmail());
        data.put("name", updatedUser.getName());
        data.put("department", updatedUser.getDepartment());
        data.put("position", updatedUser.getPosition());
        data.put("phone", updatedUser.getPhone());
        data.put("role", updatedUser.getRole());
        data.put("last_login_at", updatedUser.getLastLoginAt());
        data.put("created_at", updatedUser.getCreatedAt());
        data.put("updated_at", updatedUser.getUpdatedAt());
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * パスワードを変更するエンドポイント
     */
    @PutMapping("/password")
    @Operation(summary = "パスワード変更", description = "ログインユーザーのパスワードを変更します。")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "パスワード変更成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "認証エラー", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        
        authenticationService.changePassword(
                passwordChangeRequest.getCurrentPassword(),
                passwordChangeRequest.getNewPassword());
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "パスワードが正常に変更されました。");
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * パスワードリセット要求を送信するエンドポイント
     */
    @PostMapping("/password/reset-request")
    @Operation(summary = "パスワードリセット要求", description = "パスワードリセットのリクエストを送信し、リセット用のメールを送信します。")
    public ResponseEntity<Map<String, Object>> requestPasswordReset(
            @Valid @RequestBody Map<String, String> request) {
        
        String email = request.get("email");
        authenticationService.initiatePasswordReset(email);
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "パスワードリセット用のメールを送信しました。メールに記載されている手順に従ってください。");
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * MFAコードを検証するエンドポイント
     */
    @PostMapping("/mfa/verify")
    @Operation(summary = "MFAコード検証", description = "MFA認証コードを検証してJWTトークンを取得します")
    public ResponseEntity<Map<String, Object>> verifyMfaCode(
            @Valid @RequestBody MfaVerificationRequest mfaRequest,
            HttpServletRequest request) {

        mfaRequest.setIpAddress(getClientIp(request));
        AuthenticationResponse authResponse = authenticationService.verifyMfaCode(mfaRequest);

        // レスポンスをOpenAPI仕様に合わせてフォーマット
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        data.put("access_token", authResponse.getAccessToken());
        data.put("refresh_token", authResponse.getRefreshToken());
        data.put("token_type", authResponse.getTokenType());
        data.put("expires_in", authResponse.getExpiresIn());
        
        if (authResponse.getUser() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", authResponse.getUser().getId());
            userInfo.put("email", authResponse.getUser().getEmail());
            userInfo.put("name", authResponse.getUser().getName());
            userInfo.put("role", authResponse.getUser().getRole());
            data.put("user", userInfo);
        }
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * MFA設定を行うエンドポイント
     */
    @PostMapping("/mfa/setup")
    @Operation(summary = "MFA設定", description = "多要素認証の設定情報を取得します")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> setupMfa() {
        // 現在のユーザーIDを取得
        UUID userId = authenticationService.getCurrentUserId();
        MfaSetupResponse setupResponse = authenticationService.setupMfa(userId);
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        data.put("secret", setupResponse.getSecret());
        data.put("qr_code_uri", setupResponse.getQrCodeUri());
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * MFA設定を有効化するエンドポイント
     */
    @PostMapping("/mfa/enable")
    @Operation(summary = "MFA有効化", description = "多要素認証を有効化します")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> enableMfa(@Valid @RequestBody MfaEnableRequest request) {
        // 現在のユーザーIDを取得
        UUID userId = authenticationService.getCurrentUserId();
        authenticationService.verifyAndEnableMfa(userId, request.getMfaCode());
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "多要素認証が正常に有効化されました。");
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * MFA設定を無効化するエンドポイント
     */
    @PostMapping("/mfa/disable")
    @Operation(summary = "MFA無効化", description = "多要素認証を無効化します")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> disableMfa(@Valid @RequestBody MfaDisableRequest request) {
        // 現在のユーザーIDを取得
        UUID userId = authenticationService.getCurrentUserId();
        authenticationService.disableMfa(userId, request.getPassword());
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "多要素認証が正常に無効化されました。");
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * クライアントのIPアドレスを取得するヘルパーメソッド
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}