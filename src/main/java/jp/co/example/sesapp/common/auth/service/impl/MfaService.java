package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.domain.dto.MfaSetupResponse;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * 多要素認証（MFA）サービス
 * TOTPアルゴリズムを使用した認証コードの生成と検証を担当
 */
@Service
public class MfaService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int SECRET_BYTES = 20;  // 160ビット
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int ALLOWED_TIME_STEP_DRIFT = 1; // 前後の時間ステップも許容

    @Value("${application.security.mfa.issuer:ses-mgr}")
    private String issuer;

    @Value("${application.security.mfa.code-validity-seconds:30}")
    private int timeStepSeconds;

    /**
     * MFAセットアップ情報を生成
     *
     * @param user ユーザー
     * @return MFAセットアップレスポンス
     */
    public MfaSetupResponse generateMfaSetup(User user) {
        String secret = generateSecretKey();
        
        MfaSetupResponse response = new MfaSetupResponse();
        response.setSecret(secret);
        response.setQrCodeUri(generateQrCodeUri(user.getEmail(), secret));
        
        return response;
    }

    /**
     * 安全なTOTPシークレットキーを生成
     *
     * @return Base32エンコードされたシークレットキー
     */
    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_BYTES];
        random.nextBytes(bytes);
        
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    /**
     * QRコード用のOTAuthURIを生成
     *
     * @param username ユーザー名（メールアドレス）
     * @param secret シークレットキー
     * @return otpauth URI
     */
    public String generateQrCodeUri(String username, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer,
                username,
                secret,
                issuer,
                VERIFICATION_CODE_LENGTH,
                timeStepSeconds
        );
    }

    /**
     * TOTPコードを生成
     *
     * @param secret Base32エンコードされたシークレット
     * @return 生成されたTOTPコード
     */
    public String generateTotpCode(String secret) {
        return generateTotpCode(secret, getCurrentTimeStep());
    }

    /**
     * 特定の時間ステップに対するTOTPコードを生成
     *
     * @param secret Base32エンコードされたシークレット
     * @param timeStep 時間ステップ
     * @return 生成されたTOTPコード
     */
    public String generateTotpCode(String secret, long timeStep) {
        try {
            Base32 base32 = new Base32();
            byte[] secretBytes = base32.decode(secret.toUpperCase());
            
            byte[] timeBytes = new byte[8];
            for (int i = 7; i >= 0; i--) {
                timeBytes[i] = (byte) (timeStep & 0xff);
                timeStep >>= 8;
            }
            
            SecretKeySpec signKey = new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signKey);
            byte[] hash = mac.doFinal(timeBytes);
            
            // ダイナミックトランケーション
            int offset = hash[hash.length - 1] & 0xf;
            int binary =
                    ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);
            
            int otp = binary % (int) Math.pow(10, VERIFICATION_CODE_LENGTH);
            return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", otp);
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error generating TOTP code", e);
        }
    }

    /**
     * 現在のUNIX時間から時間ステップを計算
     *
     * @return 現在の時間ステップ
     */
    protected long getCurrentTimeStep() {
        return Instant.now().getEpochSecond() / timeStepSeconds;
    }

    /**
     * ユーザーが提供したTOTPコードを検証
     *
     * @param secret ユーザーのシークレット
     * @param inputCode ユーザーが入力したコード
     * @return 検証結果
     */
    public boolean verifyCode(String secret, String inputCode) {
        if (inputCode == null || inputCode.length() != VERIFICATION_CODE_LENGTH) {
            return false;
        }
        
        // 現在の時間ステップと前後の時間ステップでコードを検証
        long currentTimeStep = getCurrentTimeStep();
        
        for (int i = -ALLOWED_TIME_STEP_DRIFT; i <= ALLOWED_TIME_STEP_DRIFT; i++) {
            String expectedCode = generateTotpCode(secret, currentTimeStep + i);
            if (expectedCode.equals(inputCode)) {
                return true;
            }
        }
        
        return false;
    }
}