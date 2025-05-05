package com.ses_mgr.common.controller.api.simplified;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.controller.api.AuthController;
import com.ses_mgr.common.dto.LoginRequestDto;
import com.ses_mgr.common.dto.LoginResponseDto;
import com.ses_mgr.common.entity.User;
import com.ses_mgr.common.service.UserManagementService;
import com.ses_mgr.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SimplifiedAuthControllerIntegrationTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // モックの設定
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("test.user");
        loginRequest.setPassword("Password123");

        // ユーザー情報
        User testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setName("Test User");
        testUser.setLoginId("test.user");

        // Authentication のモック
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // JWT トークンの生成をモック
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("test-jwt-token");
        when(jwtTokenProvider.getRolesFromAuthentication(authentication))
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // テスト実行
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.roles[0]").value("USER"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // モックの設定
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("test.user");
        loginRequest.setPassword("WrongPassword");

        // 認証失敗をシミュレート
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // テスト実行
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithEmptyLoginId_ShouldReturnBadRequest() throws Exception {
        // モックの設定
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("");
        loginRequest.setPassword("Password123");

        // テスト実行
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // モックの設定
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginId("test.user");
        loginRequest.setPassword("");

        // テスト実行
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}