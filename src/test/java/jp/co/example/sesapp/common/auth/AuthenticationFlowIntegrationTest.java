package jp.co.example.sesapp.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.example.sesapp.common.auth.domain.AuthEventType;
import jp.co.example.sesapp.common.auth.domain.PasswordResetToken;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.domain.dto.AuthenticationResponse;
import jp.co.example.sesapp.common.auth.domain.dto.Credentials;
import jp.co.example.sesapp.common.auth.domain.dto.MfaSetupResponse;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.auth.service.AuthenticationService;
import jp.co.example.sesapp.common.auth.service.JwtProvider;
import jp.co.example.sesapp.common.auth.service.PasswordResetService;
import jp.co.example.sesapp.common.auth.service.impl.MfaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private MfaService mfaService;

    private User testUser;
    private final String testEmail = "test@example.com";
    private final String testPassword = "Password123!";
    private final String testAccessToken = "test-access-token";
    private final String testRefreshToken = "test-refresh-token";
    private final String testMfaSecret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private final String testMfaCode = "123456";
    private final String testResetToken = "reset-token-12345";

    @BeforeEach
    void setUp() {
        // テストユーザー設定
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .name("Test User")
                .passwordHash("hashed_password")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .roleId(UUID.randomUUID())
                .build();

        // デフォルトのユーザーレスポンス設定
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // 認証情報の準備
        Credentials credentials = new Credentials();
        credentials.setUsernameOrEmail(testEmail);
        credentials.setPassword(testPassword);

        // 認証レスポンスの準備
        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setAccessToken(testAccessToken);
        authResponse.setRefreshToken(testRefreshToken);
        authResponse.setTokenType("Bearer");
        authResponse.setExpiresIn(1800);
        authResponse.setRequiresMfa(false);
        
        // UserDtoの作成
        jp.co.example.sesapp.common.auth.domain.dto.UserDto userDto = new jp.co.example.sesapp.common.auth.domain.dto.UserDto();
        userDto.setId(testUser.getId());
        userDto.setEmail(testUser.getEmail());
        userDto.setUsername(testUser.getUsername());
        userDto.setFirstName(testUser.getFirstName());
        userDto.setLastName(testUser.getLastName());
        authResponse.setUser(userDto);

        // モックの設定
        when(authenticationService.authenticate(any(Credentials.class), anyString(), anyString(), anyString()))
                .thenReturn(authResponse);

        // APIリクエスト実行
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access_token").value(testAccessToken))
                .andExpect(jsonPath("$.data.refresh_token").value(testRefreshToken))
                .andExpect(jsonPath("$.data.token_type").value("Bearer"))
                .andExpect(jsonPath("$.data.expires_in").value(1800))
                .andExpect(jsonPath("$.data.user").exists())
                .andExpect(jsonPath("$.requires_mfa").doesNotExist())
                .andReturn();

        // レスポンスの検証
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains(testAccessToken);
    }

    @Test
    void login_WithMfaEnabled_ShouldReturnMfaChallenge() throws Exception {
        // 認証情報の準備
        Credentials credentials = new Credentials();
        credentials.setUsernameOrEmail(testEmail);
        credentials.setPassword(testPassword);

        // MFA挑戦の準備
        UUID challengeId = UUID.randomUUID();
        jp.co.example.sesapp.common.auth.domain.dto.MfaChallenge mfaChallenge = new jp.co.example.sesapp.common.auth.domain.dto.MfaChallenge();
        mfaChallenge.setChallengeId(challengeId);
        mfaChallenge.setMethod(jp.co.example.sesapp.common.auth.domain.AuthenticationMethod.TOTP);
        mfaChallenge.setExpiresIn(300);
        mfaChallenge.setUserId(testUser.getId());

        // 認証レスポンスの準備
        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setRequiresMfa(true);
        authResponse.setMfaChallenge(mfaChallenge);
        
        // UserDtoの作成
        jp.co.example.sesapp.common.auth.domain.dto.UserDto userDto = new jp.co.example.sesapp.common.auth.domain.dto.UserDto();
        userDto.setId(testUser.getId());
        userDto.setEmail(testUser.getEmail());
        userDto.setUsername(testUser.getUsername());
        userDto.setFirstName(testUser.getFirstName());
        userDto.setLastName(testUser.getLastName());
        authResponse.setUser(userDto);

        // モックの設定
        when(authenticationService.authenticate(any(Credentials.class), anyString(), anyString(), anyString()))
                .thenReturn(authResponse);

        // APIリクエスト実行
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials))
                .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requires_mfa").value(true))
                .andExpect(jsonPath("$.mfa_challenge").exists())
                .andExpect(jsonPath("$.data.user").exists())
                .andExpect(jsonPath("$.data.access_token").doesNotExist());
    }

    @Test
    void verifyMfaCode_WithValidCode_ShouldReturnTokens() throws Exception {
        // MFA検証リクエストの準備
        Map<String, String> mfaRequest = new HashMap<>();
        mfaRequest.put("challengeId", UUID.randomUUID().toString());
        mfaRequest.put("code", testMfaCode);
        mfaRequest.put("deviceId", "test-device");

        // 認証レスポンスの準備
        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setAccessToken(testAccessToken);
        authResponse.setRefreshToken(testRefreshToken);
        authResponse.setTokenType("Bearer");
        authResponse.setExpiresIn(1800);
        
        // UserDtoの作成
        jp.co.example.sesapp.common.auth.domain.dto.UserDto userDto = new jp.co.example.sesapp.common.auth.domain.dto.UserDto();
        userDto.setId(testUser.getId());
        userDto.setEmail(testUser.getEmail());
        userDto.setUsername(testUser.getUsername());
        userDto.setFirstName(testUser.getFirstName());
        userDto.setLastName(testUser.getLastName());
        authResponse.setUser(userDto);

        // モックの設定
        when(authenticationService.verifyMfaCode(any()))
                .thenReturn(authResponse);

        // APIリクエスト実行
        mockMvc.perform(post("/api/auth/mfa/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access_token").value(testAccessToken))
                .andExpect(jsonPath("$.data.refresh_token").value(testRefreshToken))
                .andExpect(jsonPath("$.data.token_type").value("Bearer"))
                .andExpect(jsonPath("$.data.expires_in").value(1800))
                .andExpect(jsonPath("$.data.user").exists());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAccessToken() throws Exception {
        // リフレッシュトークンリクエストの準備
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", testRefreshToken);

        // 認証レスポンスの準備
        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setAccessToken("new-access-token");
        authResponse.setTokenType("Bearer");
        authResponse.setExpiresIn(1800);

        // モックの設定
        when(authenticationService.refreshToken(eq(testRefreshToken), anyString(), anyString(), anyString()))
                .thenReturn(authResponse);

        // APIリクエスト実行
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access_token").value("new-access-token"))
                .andExpect(jsonPath("$.data.token_type").value("Bearer"))
                .andExpect(jsonPath("$.data.expires_in").value(1800));
    }

    @Test
    void logout_WithValidToken_ShouldSucceed() throws Exception {
        // ログアウトリクエストの準備
        Map<String, String> logoutRequest = new HashMap<>();
        logoutRequest.put("refreshToken", testRefreshToken);

        // モックの設定
        doNothing().when(authenticationService).logout(testRefreshToken);

        // APIリクエスト実行
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + testAccessToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    @WithMockUser
    void setupMfa_WhenAuthenticated_ShouldReturnSetupInfo() throws Exception {
        // MFA設定レスポンスの準備
        MfaSetupResponse setupResponse = new MfaSetupResponse();
        setupResponse.setSecret(testMfaSecret);
        setupResponse.setQrCodeUri("otpauth://totp/test-ses-mgr:test@example.com?secret=" + testMfaSecret);

        // モックの設定
        when(authenticationService.getCurrentUserId()).thenReturn(testUser.getId());
        when(authenticationService.setupMfa(testUser.getId())).thenReturn(setupResponse);

        // APIリクエスト実行
        mockMvc.perform(post("/api/auth/mfa/setup")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.secret").value(testMfaSecret))
                .andExpect(jsonPath("$.data.qr_code_uri").exists());
    }

    @Test
    @WithMockUser
    void enableMfa_WithValidCode_ShouldEnableMfa() throws Exception {
        // MFA有効化リクエストの準備
        Map<String, String> enableRequest = new HashMap<>();
        enableRequest.put("mfaCode", testMfaCode);

        // モックの設定
        when(authenticationService.getCurrentUserId()).thenReturn(testUser.getId());
        doNothing().when(authenticationService).verifyAndEnableMfa(testUser.getId(), testMfaCode);

        // APIリクエスト実行
        mockMvc.perform(post("/api/auth/mfa/enable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enableRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("多要素認証が正常に有効化されました。"));
    }

    @Test
    @WithMockUser
    void disableMfa_WithValidPassword_ShouldDisableMfa() throws Exception {
        // MFA無効化リクエストの準備
        Map<String, String> disableRequest = new HashMap<>();
        disableRequest.put("password", testPassword);

        // モックの設定
        when(authenticationService.getCurrentUserId()).thenReturn(testUser.getId());
        doNothing().when(authenticationService).disableMfa(testUser.getId(), testPassword);

        // APIリクエスト実行
        mockMvc.perform(post("/api/auth/mfa/disable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(disableRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("多要素認証が正常に無効化されました。"));
    }

    @Test
    void requestPasswordReset_WithValidEmail_ShouldInitiateReset() throws Exception {
        // パスワードリセットリクエストの準備
        Map<String, String> resetRequest = new HashMap<>();
        resetRequest.put("email", testEmail);

        // パスワードリセットトークンの準備
        PasswordResetToken resetToken = PasswordResetToken.createToken(testUser.getId(), 30);
        resetToken.setToken(testResetToken);

        // モックの設定
        doNothing().when(authenticationService).initiatePasswordReset(testEmail);

        // APIリクエスト実行
        mockMvc.perform(post("/api/auth/password/reset-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    @WithMockUser
    void getUserProfile_WhenAuthenticated_ShouldReturnProfile() throws Exception {
        // ユーザーDTOの準備
        when(authenticationService.getCurrentUserProfile()).thenReturn(
                new jp.co.example.sesapp.common.auth.domain.dto.UserDto(
                        testUser.getId(),
                        testUser.getEmail(),
                        testUser.getName(),
                        "IT部門",
                        "エンジニア",
                        "03-1234-5678",
                        "USER",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        // APIリクエスト実行
        mockMvc.perform(get("/api/auth/profile")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.data.name").value(testUser.getName()))
                .andExpect(jsonPath("$.data.department").value("IT部門"))
                .andExpect(jsonPath("$.data.position").value("エンジニア"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @WithMockUser
    void updateProfile_WithValidData_ShouldUpdateProfile() throws Exception {
        // プロフィール更新リクエストの準備
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("name", "Updated Name");
        updateRequest.put("department", "Updated Department");
        updateRequest.put("position", "Updated Position");
        updateRequest.put("phone", "03-9876-5432");

        // 更新後のユーザーDTOの準備
        when(authenticationService.updateUserProfile(any())).thenReturn(
                new jp.co.example.sesapp.common.auth.domain.dto.UserDto(
                        testUser.getId(),
                        testUser.getEmail(),
                        "Updated Name",
                        "Updated Department",
                        "Updated Position",
                        "03-9876-5432",
                        "USER",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusSeconds(1)
                )
        );

        // APIリクエスト実行
        mockMvc.perform(put("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.department").value("Updated Department"))
                .andExpect(jsonPath("$.data.position").value("Updated Position"))
                .andExpect(jsonPath("$.data.phone").value("03-9876-5432"));
    }

    @Test
    @WithMockUser
    void changePassword_WithValidData_ShouldChangePassword() throws Exception {
        // パスワード変更リクエストの準備
        Map<String, String> changeRequest = new HashMap<>();
        changeRequest.put("currentPassword", testPassword);
        changeRequest.put("newPassword", "NewPassword123!");

        // モックの設定
        doNothing().when(authenticationService).changePassword(testPassword, "NewPassword123!");

        // APIリクエスト実行
        mockMvc.perform(put("/api/auth/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("パスワードが正常に変更されました。"));
    }
}