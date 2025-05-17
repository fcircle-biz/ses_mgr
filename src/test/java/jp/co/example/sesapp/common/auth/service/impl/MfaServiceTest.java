package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.domain.dto.MfaSetupResponse;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Spy
    @InjectMocks
    private MfaService mfaService;

    private User testUser;
    private final String issuer = "test-ses-mgr";
    private final int timeStepSeconds = 30;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mfaService, "issuer", issuer);
        ReflectionTestUtils.setField(mfaService, "timeStepSeconds", timeStepSeconds);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .build();
    }

    @Test
    void generateSecretKey_ShouldGenerateBase32EncodedString() {
        // 実行
        String secret = mfaService.generateSecretKey();

        // 検証
        assertThat(secret).isNotNull().isNotEmpty();
        
        // Base32エンコードの形式検証（A-Z, 2-7のみを含み、=でパディングされる可能性あり）
        assertThat(secret.replaceAll("[A-Z2-7=]", "")).isEmpty();
        
        // 長さの検証（160ビット = 20バイトのバイナリ → Base32で最大32文字+パディング）
        assertThat(secret.length()).isGreaterThanOrEqualTo(32);
        
        // デコード可能の検証
        Base32 base32 = new Base32();
        byte[] decoded = base32.decode(secret);
        assertThat(decoded.length).isGreaterThanOrEqualTo(20); // 20バイト以上（パディングにより少し異なる可能性あり）
    }

    @Test
    void generateQrCodeUri_ShouldReturnValidOtpauthUri() {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        
        // 実行
        String uri = mfaService.generateQrCodeUri(testUser.getEmail(), secret);
        
        // 検証
        String expectedUri = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=%d",
                issuer,
                testUser.getEmail(),
                secret,
                issuer,
                timeStepSeconds
        );
        
        assertThat(uri).isEqualTo(expectedUri);
    }

    @Test
    void generateMfaSetup_ShouldReturnCompleteSetupInfo() {
        // 準備
        String testSecret = "TESTSECRET123456";
        String testQrUri = "otpauth://totp/test-ses-mgr:test@example.com?secret=TESTSECRET123456&issuer=test-ses-mgr&algorithm=SHA1&digits=6&period=30";
        
        doReturn(testSecret).when(mfaService).generateSecretKey();
        doReturn(testQrUri).when(mfaService).generateQrCodeUri(eq(testUser.getEmail()), eq(testSecret));
        
        // 実行
        MfaSetupResponse response = mfaService.generateMfaSetup(testUser);
        
        // 検証
        assertThat(response).isNotNull();
        assertThat(response.getSecret()).isEqualTo(testSecret);
        assertThat(response.getQrCodeUri()).isEqualTo(testQrUri);
        
        // メソッド呼び出し検証
        verify(mfaService).generateSecretKey();
        verify(mfaService).generateQrCodeUri(testUser.getEmail(), testSecret);
    }

    @Test
    void generateTotpCode_ShouldGenerateSixDigitCode() {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        
        // 実行
        String code = mfaService.generateTotpCode(secret);
        
        // 検証
        assertThat(code).isNotNull();
        assertThat(code).hasSize(6);
        assertThat(code).matches("\\d{6}"); // 6桁の数字であること
    }

    @Test
    void generateTotpCode_WithSameSecretAndTimeStep_ShouldGenerateSameCode() {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        long timeStep = 12345L; // 任意の時間ステップ
        
        // 実行
        String code1 = mfaService.generateTotpCode(secret, timeStep);
        String code2 = mfaService.generateTotpCode(secret, timeStep);
        
        // 検証
        assertThat(code1).isEqualTo(code2);
    }

    @Test
    void generateTotpCode_WithDifferentTimeSteps_ShouldGenerateDifferentCodes() {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        long timeStep1 = 12345L;
        long timeStep2 = 12346L;
        
        // 実行
        String code1 = mfaService.generateTotpCode(secret, timeStep1);
        String code2 = mfaService.generateTotpCode(secret, timeStep2);
        
        // 検証
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void generateTotpCode_WithDifferentSecrets_ShouldGenerateDifferentCodes() {
        // 準備
        String secret1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        String secret2 = "ZYXWVUTSRQPONMLKJIHGFEDCBA765432";
        long timeStep = 12345L;
        
        // 実行
        String code1 = mfaService.generateTotpCode(secret1, timeStep);
        String code2 = mfaService.generateTotpCode(secret2, timeStep);
        
        // 検証
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void verifyTotpCode_WithValidCode_ShouldReturnTrue() {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        long currentTimeStep = Instant.now().getEpochSecond() / timeStepSeconds;
        
        // モック: 現在の時間ステップを固定
        // getCurrentTimeStepはprivateメソッドなのでリフレクションを使用せず、
        // verifyCodeメソッドを直接使用してテストを書き換える
        
        // 生成されたコードを取得
        String validCode = mfaService.generateTotpCode(secret, currentTimeStep);
        
        // 実行と検証
        assertThat(mfaService.verifyCode(secret, validCode)).isTrue();
    }

    @Test
    void verifyTotpCode_WithPreviousTimeStepCode_ShouldReturnTrue() throws Exception {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        long currentTimeStep = 12345L;
        
        // テスト用のサブクラスを作成してprivateメソッドの動作をオーバーライド
        MfaService testMfaService = new MfaService() {
            // privateメソッドをオーバーライドするためにprotectedとして再定義
            @Override
            protected long getCurrentTimeStep() {
                return currentTimeStep;
            }
        };
        
        // ReflectionTestUtilsを使ってprivateフィールドを設定
        ReflectionTestUtils.setField(testMfaService, "timeStepSeconds", 30);
        ReflectionTestUtils.setField(testMfaService, "issuer", "test-ses-mgr");
        
        // 1つ前の時間ステップのコードを生成
        String previousCode = testMfaService.generateTotpCode(secret, currentTimeStep - 1);
        
        // 実行と検証 - 前後の時間ステップを含む時間枠内なので有効
        assertThat(testMfaService.verifyCode(secret, previousCode)).isTrue();
    }

    @Test
    void verifyTotpCode_WithNextTimeStepCode_ShouldReturnTrue() throws Exception {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        long currentTimeStep = 12345L;
        
        // テスト用のサブクラスを作成してprivateメソッドの動作をオーバーライド
        MfaService testMfaService = new MfaService() {
            // getCurrentTimeStepメソッドをオーバーライド
            @Override
            protected long getCurrentTimeStep() {
                return currentTimeStep;
            }
        };
        
        // ReflectionTestUtilsを使ってprivateフィールドを設定
        ReflectionTestUtils.setField(testMfaService, "timeStepSeconds", 30);
        ReflectionTestUtils.setField(testMfaService, "issuer", "test-ses-mgr");
        
        // 1つ次の時間ステップのコードを生成
        String nextCode = testMfaService.generateTotpCode(secret, currentTimeStep + 1);
        
        // 実行と検証 - 前後の時間ステップを含む時間枠内なので有効
        assertThat(testMfaService.verifyCode(secret, nextCode)).isTrue();
    }

    @Test
    void verifyTotpCode_WithOutOfRangeTimeStepCode_ShouldReturnFalse() {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        long currentTimeStep = 12345L;
        
        // テスト用のサブクラスを作成してprivateメソッドの動作をオーバーライド
        MfaService testMfaService = new MfaService() {
            // getCurrentTimeStepメソッドをオーバーライド
            @Override
            protected long getCurrentTimeStep() {
                return currentTimeStep;
            }
        };
        
        // ReflectionTestUtilsを使ってprivateフィールドを設定
        ReflectionTestUtils.setField(testMfaService, "timeStepSeconds", 30);
        ReflectionTestUtils.setField(testMfaService, "issuer", "test-ses-mgr");
        
        // 2つ前の時間ステップのコード（許容範囲外）を生成
        String invalidCode = testMfaService.generateTotpCode(secret, currentTimeStep - 2);
        
        // 実行と検証
        assertThat(testMfaService.verifyCode(secret, invalidCode)).isFalse();
    }

    @Test
    void verifyTotpCode_WithInvalidLength_ShouldReturnFalse() {
        // 準備
        String secret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        
        // 実行と検証 - 短すぎるコード
        assertThat(mfaService.verifyCode(secret, "12345")).isFalse();
        
        // 実行と検証 - 長すぎるコード
        assertThat(mfaService.verifyCode(secret, "1234567")).isFalse();
        
        // 実行と検証 - nullコード
        assertThat(mfaService.verifyCode(secret, null)).isFalse();
    }
}