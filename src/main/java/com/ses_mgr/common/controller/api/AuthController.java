package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.RefreshToken;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.service.PasswordResetService;
import com.ses_mgr.common.service.RefreshTokenService;
import com.ses_mgr.common.service.UserService;
import com.ses_mgr.config.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final ObjectMapper objectMapper;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            UserService userService,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetService = passwordResetService;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            // 実際の認証処理を実装
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getLoginId(),
                            loginRequest.getPassword()
                    )
            );
            
            // 認証情報をセキュリティコンテキストに設定
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 認証されたユーザー情報を取得
            User user = (User) authentication.getPrincipal();
            UUID userId = user.getUserId();
            
            try {
                // 最終ログイン時間の更新
                userService.updateLastLoginTime(userId);
            } catch (Exception e) {
                // 最終ログイン時間の更新に失敗しても、認証は続行
            }

            // ユーザーの権限（ロール）を取得
            List<String> roles = user.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());

            // JWTトークンを生成
            String jwt = tokenProvider.generateToken(user);
            
            // リフレッシュトークンを生成（remember_me対応）
            boolean rememberMe = loginRequest.isRememberMe();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId(), rememberMe);

            // ユーザーロールを設定
            String userRole = roles.isEmpty() ? "USER" : roles.get(0);
            
            // レスポンスを直接マップで構築（JsonNodeで構築してもよい）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", jwt != null ? jwt : "dummy-token");
            responseData.put("refreshToken", refreshToken.getToken());
            responseData.put("tokenType", "Bearer");
            responseData.put("expiresIn", jwtExpirationInMs / 1000); // 秒単位
            responseData.put("userId", user.getUserId().toString());
            responseData.put("name", user.getName());
            responseData.put("roles", Collections.singletonList(userRole));
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", responseData);
            
            // レスポンスを直接JSON文字列で返す
            try {
                String jsonResponse = objectMapper.writeValueAsString(response);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonResponse);
            } catch (Exception e) {
                // 万が一JSONシリアライズに失敗した場合は従来の方法でレスポンスを返す
                LoginResponseDto loginResponse = LoginResponseDto.builder()
                        .accessToken(jwt)
                        .tokenType("Bearer")
                        .userId(user.getUserId())
                        .name(user.getName())
                        .roles(Collections.singletonList(userRole))
                        .build();
                
                return ResponseEntity.ok(ApiResponseDto.success(loginResponse));
            }
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // 認証失敗の場合は401を返す
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("UNAUTHORIZED", "Invalid credentials"));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // ユーザーが見つからない場合も401を返す
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("UNAUTHORIZED", "User not found"));
        } catch (Exception e) {
            // その他の例外が発生した場合は500を返す
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "An internal server error occurred"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequestDto refreshRequest) {
        try {
            String requestRefreshToken = refreshRequest.getRefreshToken();

            // リフレッシュトークンを検証
            Optional<RefreshToken> refreshTokenOpt = refreshTokenService.validateAndGetRefreshToken(requestRefreshToken);
            if (refreshTokenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDto.error("INVALID_REFRESH_TOKEN", "リフレッシュトークンが無効または期限切れです。"));
            }

            RefreshToken refreshToken = refreshTokenOpt.get();
            User user = refreshToken.getUser();

            // 新しいアクセストークンを生成
            String newAccessToken = tokenProvider.generateToken(user);

            // レスポンスデータを構築
            TokenRefreshResponseDto responseDto = TokenRefreshResponseDto.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpirationInMs / 1000) // 秒単位
                    .build();

            return ResponseEntity.ok(ApiResponseDto.success(responseDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "トークンの更新中にエラーが発生しました。"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDto.error("UNAUTHORIZED", "認証されていないか、セッションが無効です。"));
            }
            
            UUID userId = currentUser.getUserId();
            
            // リフレッシュトークンも一緒に送られてきた場合は、そのトークンのみを無効化
            if (body != null && StringUtils.hasText(body.get("refreshToken"))) {
                refreshTokenService.revokeRefreshToken(body.get("refreshToken"));
            } else {
                // リフレッシュトークンが指定されていない場合は、ユーザーの全てのリフレッシュトークンを無効化
                refreshTokenService.revokeAllUserTokens(userId);
            }

            // セキュリティコンテキストをクリア
            SecurityContextHolder.clearContext();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "ログアウトが完了しました。");
            
            return ResponseEntity.ok(ApiResponseDto.success(responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "ログアウト処理中にエラーが発生しました。"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User currentUser) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDto.error("UNAUTHORIZED", "認証されていないか、セッションが無効です。"));
            }
            
            UserProfileResponseDto profileDto = convertToProfileDto(currentUser);
            return ResponseEntity.ok(ApiResponseDto.success(profileDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "プロフィール取得中にエラーが発生しました。"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequestDto updateRequest,
            @AuthenticationPrincipal User currentUser) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDto.error("UNAUTHORIZED", "認証されていないか、セッションが無効です。"));
            }

            UserUpdateRequestDto userUpdateDto = UserUpdateRequestDto.builder()
                    .name(updateRequest.getName())
                    .department(updateRequest.getDepartment())
                    .position(updateRequest.getPosition())
                    .phone(updateRequest.getPhone())
                    .build();

            UserResponseDto updatedUser = userService.updateUser(currentUser.getUserId(), userUpdateDto);
            
            // 更新後の完全なユーザー情報を取得
            User refreshedUser = (User) userService.loadUserByUsername(currentUser.getUsername());
            UserProfileResponseDto profileDto = convertToProfileDto(refreshedUser);

            return ResponseEntity.ok(ApiResponseDto.success(profileDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("NOT_FOUND", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "プロフィール更新中にエラーが発生しました。"));
        }
    }
    
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody PasswordChangeRequestDto passwordChangeRequest,
            @AuthenticationPrincipal User currentUser) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDto.error("UNAUTHORIZED", "認証されていないか、セッションが無効です。"));
            }
            
            // 新しいパスワードと確認用パスワードが一致するか確認
            if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getNewPasswordConfirmation())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("VALIDATION_ERROR", "新しいパスワードと確認用パスワードが一致しません。"));
            }
            
            // パスワード変更処理を実行
            userService.changePassword(
                currentUser.getUserId(),
                passwordChangeRequest.getCurrentPassword(),
                passwordChangeRequest.getNewPassword()
            );
            
            // 成功レスポンスを返す
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "パスワードが正常に変更されました。");
            return ResponseEntity.ok(ApiResponseDto.success(responseData));
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("NOT_FOUND", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("INVALID_CURRENT_PASSWORD", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "パスワード変更中にエラーが発生しました。"));
        }
    }

    /**
     * Authorizationヘッダーからトークンを抽出
     * @param authHeader Authorizationヘッダー
     * @return トークン文字列（ない場合はnull）
     */
    private String extractTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * パスワードリセット要求エンドポイント
     */
    @PostMapping("/password/reset-request")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto resetRequest) {
        try {
            // リセットリクエストの処理
            try {
                passwordResetService.createPasswordResetRequest(resetRequest.getEmail());
            } catch (EntityNotFoundException e) {
                // ユーザーが見つからない場合でも成功レスポンスを返す（セキュリティ上の理由）
                // クライアントに対してメールアドレスが存在するかどうかの情報を漏らさない
            }

            // 常に成功レスポンスを返す
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "パスワードリセット用のメールを送信しました。メールに記載されている手順に従ってください。");
            return ResponseEntity.ok(ApiResponseDto.success(responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "パスワードリセット要求の処理中にエラーが発生しました。"));
        }
    }

    /**
     * パスワードリセット実行エンドポイント
     */
    @PostMapping("/password/reset")
    public ResponseEntity<?> executePasswordReset(@Valid @RequestBody PasswordResetExecuteRequestDto resetExecuteRequest) {
        try {
            // 新しいパスワードと確認用パスワードが一致するか確認
            if (!resetExecuteRequest.getPassword().equals(resetExecuteRequest.getPasswordConfirmation())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("VALIDATION_ERROR", "パスワードとパスワード確認が一致しません。"));
            }

            // リセットトークンを検証
            boolean isValid = passwordResetService.validatePasswordResetToken(
                    resetExecuteRequest.getToken(),
                    resetExecuteRequest.getEmail()
            );

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("INVALID_RESET_TOKEN", "パスワードリセットトークンが無効または期限切れです。"));
            }

            // パスワードリセットの実行
            boolean resetSuccess = passwordResetService.executePasswordReset(
                    resetExecuteRequest.getToken(),
                    resetExecuteRequest.getEmail(),
                    resetExecuteRequest.getPassword()
            );

            if (!resetSuccess) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("RESET_FAILED", "パスワードリセットに失敗しました。"));
            }

            // 成功レスポンスを返す
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "パスワードが正常にリセットされました。新しいパスワードでログインしてください。");
            return ResponseEntity.ok(ApiResponseDto.success(responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("SERVER_ERROR", "パスワードリセット実行中にエラーが発生しました。"));
        }
    }

    /**
     * ユーザーエンティティからプロフィールDTOに変換
     * @param user ユーザーエンティティ
     * @return ユーザープロフィールDTO
     */
    private UserProfileResponseDto convertToProfileDto(User user) {
        if (user == null) {
            return null;
        }

        // ユーザーのロールを取得
        String roleName = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .findFirst()
                .orElse(null);

        // 部署名を取得
        String departmentName = null;
        Department department = user.getDepartment();
        if (department != null) {
            departmentName = department.getDepartmentName();
        }

        return UserProfileResponseDto.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .department(departmentName)
                .position(user.getPosition())
                .phone(user.getPhone())
                .role(roleName)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}