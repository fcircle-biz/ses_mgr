package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.ApiResponseDto;
import com.ses_mgr.common.dto.LoginRequestDto;
import com.ses_mgr.common.dto.LoginResponseDto;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.service.UserManagementService;
import com.ses_mgr.config.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserManagementService userManagementService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            UserManagementService userManagementService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userManagementService = userManagementService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        // 認証を実行
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getLoginId(),
                        loginRequest.getPassword()
                )
        );

        // セキュリティコンテキストに認証情報を設定
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ユーザー情報を取得
        User user = (User) authentication.getPrincipal();

        // 最終ログイン時間の更新
        userManagementService.updateLastLoginTime(user.getUserId());

        // JWTトークンを生成
        String jwt = tokenProvider.generateToken(authentication);

        // レスポンスの作成
        LoginResponseDto loginResponse = LoginResponseDto.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .name(user.getName())
                .roles(user.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .toList())
                .build();

        return ResponseEntity.ok(ApiResponseDto.success(loginResponse));
    }
}