package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.entity.RefreshToken;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.service.PasswordResetService;
import com.ses_mgr.common.service.RefreshTokenService;
import com.ses_mgr.common.service.UserService;
import com.ses_mgr.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * シンプルなJUnit単体テスト - 認証APIのテスト
 */
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testRefreshToken() {
        // モックの設定
        TokenRefreshRequestDto refreshRequest = new TokenRefreshRequestDto();
        refreshRequest.setRefreshToken("test-refresh-token");

        User mockUser = User.builder()
            .userId(UUID.randomUUID())
            .loginId("testuser")
            .email("test@example.com")
            .name("Test User")
            .passwordHash("hashed_password")
            .build();

        RefreshToken mockRefreshToken = RefreshToken.builder()
            .tokenId(UUID.randomUUID())
            .token("test-refresh-token")
            .user(mockUser)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();

        when(refreshTokenService.validateAndGetRefreshToken(anyString()))
            .thenReturn(Optional.of(mockRefreshToken));
        when(tokenProvider.generateToken(any(User.class)))
            .thenReturn("new-access-token");

        // テスト対象メソッドを実行
        ResponseEntity<?> response = authController.refreshToken(refreshRequest);

        // 検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testPasswordResetRequest() {
        // モックの設定
        PasswordResetRequestDto resetRequest = new PasswordResetRequestDto();
        resetRequest.setEmail("test@example.com");

        when(passwordResetService.createPasswordResetRequest(anyString()))
            .thenReturn("reset-token");

        // テスト対象メソッドを実行
        ResponseEntity<?> response = authController.requestPasswordReset(resetRequest);

        // 検証
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }
}