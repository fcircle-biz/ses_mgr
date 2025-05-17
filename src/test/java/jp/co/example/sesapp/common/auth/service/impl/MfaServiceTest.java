package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.domain.dto.MfaSetupResponse;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class MfaServiceTest {

    private MfaService mfaService;
    private MfaService spyMfaService;
    private User testUser;
    private static final String TEST_ISSUER = "ses-mgr-test";
    private static final int TEST_TIME_STEP_SECONDS = 30;

    @BeforeEach
    void setUp() {
        // MfaServiceの実インスタンスを作成
        mfaService = new MfaService();
        
        // 設定値をセット
        ReflectionTestUtils.setField(mfaService, "issuer", TEST_ISSUER);
        ReflectionTestUtils.setField(mfaService, "timeStepSeconds", TEST_TIME_STEP_SECONDS);
        
        // 一部メソッドをモック化するためのスパイを作成
        spyMfaService = spy(mfaService);
        
        // テストユーザーを作成
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void generateSecretKey_ShouldGenerateBase32EncodedString() {
        // シークレットキーを生成
        String secretKey = mfaService.generateSecretKey();

        // シークレットキーがnullや空でないことを確認
        assertThat(secretKey).isNotNull().isNotEmpty();

        // Base32エンコードされた文字列であることを確認
        Base32 base32 = new Base32();
        byte[] decodedKey = base32.decode(secretKey);
        assertThat(decodedKey).isNotNull();
        assertThat(decodedKey.length).isEqualTo(20); // 160ビット = 20バイト
    }

    @Test
    void generateQrCodeUri_ShouldReturnValidOtpauthUri() {
        // QRコードURIを生成
        String secretKey = "ABCDEFGHIJKLMNOPQRST234567";
        String qrCodeUri = mfaService.generateQrCodeUri(testUser.getEmail(), secretKey);

        // QRコードURIがnullや空でないことを確認
        assertThat(qrCodeUri).isNotNull().isNotEmpty();

        // 有効なotpauth URIであることを確認（必要なパラメータを含む）
        assertThat(qrCodeUri).startsWith("otpauth://totp/");
        assertThat(qrCodeUri).contains("secret=" + secretKey);
        assertThat(qrCodeUri).contains("issuer=" + TEST_ISSUER);
        assertThat(qrCodeUri).contains("algorithm=SHA1");
        assertThat(qrCodeUri).contains("digits=6");
        assertThat(qrCodeUri).contains("period=" + TEST_TIME_STEP_SECONDS);
        assertThat(qrCodeUri).contains(testUser.getEmail());
    }

    @Test
    void generateMfaSetup_ShouldReturnCompleteSetupInfo() {
        // MFAセットアップ情報を生成（シークレットキー生成をスパイでモック化）
        String mockSecretKey = "ABCDEFGHIJKLMNOPQRST234567";
        when(spyMfaService.generateSecretKey()).thenReturn(mockSecretKey);

        // MFAセットアップを生成
        MfaSetupResponse setupResponse = spyMfaService.generateMfaSetup(testUser);

        // セットアップレスポンスを検証
        assertThat(setupResponse).isNotNull();
        assertThat(setupResponse.getSecret()).isEqualTo(mockSecretKey);
        assertThat(setupResponse.getQrCodeUri()).isNotNull().isNotEmpty();
        assertThat(setupResponse.getQrCodeUri()).contains(mockSecretKey);
        assertThat(setupResponse.getQrCodeUri()).contains(testUser.getEmail());
    }

    @Test
    void generateTotpCode_ShouldGenerateSixDigitCode() {
        // TOTPコードを生成
        String secretKey = mfaService.generateSecretKey();
        String totpCode = mfaService.generateTotpCode(secretKey);

        // TOTPコードを検証
        assertThat(totpCode).isNotNull().isNotEmpty();
        assertThat(totpCode).hasSize(6);
        assertThat(totpCode).matches("\\d{6}"); // 6桁の数字
    }

    @Test
    void generateTotpCode_WithSameSecretAndTimeStep_ShouldGenerateSameCode() {
        // 同じシークレットと時間ステップでTOTPコードを生成
        String secretKey = mfaService.generateSecretKey();
        long timeStep = 12345L; // テスト用の固定時間ステップ

        String totpCode1 = mfaService.generateTotpCode(secretKey, timeStep);
        String totpCode2 = mfaService.generateTotpCode(secretKey, timeStep);

        // コードが同じであることを確認
        assertThat(totpCode1).isEqualTo(totpCode2);
    }

    @Test
    void generateTotpCode_WithDifferentTimeSteps_ShouldGenerateDifferentCodes() {
        // 同じシークレットで異なる時間ステップのTOTPコードを生成
        String secretKey = mfaService.generateSecretKey();
        long timeStep1 = 12345L;
        long timeStep2 = 12346L;

        String totpCode1 = mfaService.generateTotpCode(secretKey, timeStep1);
        String totpCode2 = mfaService.generateTotpCode(secretKey, timeStep2);

        // コードが異なることを確認
        assertThat(totpCode1).isNotEqualTo(totpCode2);
    }

    @Test
    void generateTotpCode_WithDifferentSecrets_ShouldGenerateDifferentCodes() {
        // 異なるシークレットで同じ時間ステップのTOTPコードを生成
        String secretKey1 = mfaService.generateSecretKey();
        String secretKey2 = mfaService.generateSecretKey();
        long timeStep = 12345L;

        String totpCode1 = mfaService.generateTotpCode(secretKey1, timeStep);
        String totpCode2 = mfaService.generateTotpCode(secretKey2, timeStep);

        // コードが異なることを確認
        assertThat(totpCode1).isNotEqualTo(totpCode2);
    }

    @Test
    void verifyTotpCode_WithValidCode_ShouldReturnTrue() {
        // テスト用の固定時間ステップを設定
        long currentTimeStep = 12345L;
        when(spyMfaService.getCurrentTimeStep()).thenReturn(currentTimeStep);

        // シークレットを生成
        String secretKey = mfaService.generateSecretKey();
        
        // 現在の時間ステップで有効なTOTPコードを生成
        String validCode = mfaService.generateTotpCode(secretKey, currentTimeStep);
        
        // コードを検証
        boolean isValid = spyMfaService.verifyCode(secretKey, validCode);
        
        // コードが有効であることを確認
        assertThat(isValid).isTrue();
    }

    @Test
    void verifyTotpCode_WithPreviousTimeStepCode_ShouldReturnTrue() {
        // テスト用の固定時間ステップを設定
        long currentTimeStep = 12345L;
        when(spyMfaService.getCurrentTimeStep()).thenReturn(currentTimeStep);

        // シークレットを生成
        String secretKey = mfaService.generateSecretKey();
        
        // 前の時間ステップでTOTPコードを生成
        String previousStepCode = mfaService.generateTotpCode(secretKey, currentTimeStep - 1);
        
        // コードを検証
        boolean isValid = spyMfaService.verifyCode(secretKey, previousStepCode);
        
        // コードが有効であることを確認（許容ドリフト内）
        assertThat(isValid).isTrue();
    }

    @Test
    void verifyTotpCode_WithNextTimeStepCode_ShouldReturnTrue() {
        // テスト用の固定時間ステップを設定
        long currentTimeStep = 12345L;
        when(spyMfaService.getCurrentTimeStep()).thenReturn(currentTimeStep);

        // シークレットを生成
        String secretKey = mfaService.generateSecretKey();
        
        // 次の時間ステップでTOTPコードを生成
        String nextStepCode = mfaService.generateTotpCode(secretKey, currentTimeStep + 1);
        
        // コードを検証
        boolean isValid = spyMfaService.verifyCode(secretKey, nextStepCode);
        
        // コードが有効であることを確認（許容ドリフト内）
        assertThat(isValid).isTrue();
    }

    @Test
    void verifyTotpCode_WithOutOfRangeTimeStepCode_ShouldReturnFalse() {
        // テスト用の固定時間ステップを設定
        long currentTimeStep = 12345L;
        when(spyMfaService.getCurrentTimeStep()).thenReturn(currentTimeStep);

        // シークレットを生成
        String secretKey = mfaService.generateSecretKey();
        
        // 許容範囲外の時間ステップでTOTPコードを生成
        String pastCode = mfaService.generateTotpCode(secretKey, currentTimeStep - 2);
        String futureCode = mfaService.generateTotpCode(secretKey, currentTimeStep + 2);
        
        // コードを検証
        boolean isPastValid = spyMfaService.verifyCode(secretKey, pastCode);
        boolean isFutureValid = spyMfaService.verifyCode(secretKey, futureCode);
        
        // どちらのコードも無効であることを確認（許容ドリフト外）
        assertThat(isPastValid).isFalse();
        assertThat(isFutureValid).isFalse();
    }

    @Test
    void verifyTotpCode_WithInvalidLength_ShouldReturnFalse() {
        // シークレットを生成
        String secretKey = mfaService.generateSecretKey();
        
        // 無効な長さのコードを検証
        boolean isShortValid = mfaService.verifyCode(secretKey, "12345");  // 短すぎる
        boolean isLongValid = mfaService.verifyCode(secretKey, "1234567"); // 長すぎる
        boolean isNullValid = mfaService.verifyCode(secretKey, null);     // null
        
        // すべての無効なコードが拒否されることを確認
        assertThat(isShortValid).isFalse();
        assertThat(isLongValid).isFalse();
        assertThat(isNullValid).isFalse();
    }

    @Test
    void getCurrentTimeStep_ShouldCalculateTimeStep() {
        // 現在の時間ステップを取得
        long timeStep = mfaService.getCurrentTimeStep();
        
        // 現在時刻に基づいて妥当な値であることを確認
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long expectedTimeStep = currentTimeSeconds / TEST_TIME_STEP_SECONDS;
        
        // テスト実行時間により若干のずれがあるため、近い値であることを確認
        assertThat(timeStep).isCloseTo(expectedTimeStep, org.assertj.core.data.Offset.offset(1L));
    }
}