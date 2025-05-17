package jp.co.example.sesapp.common.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 多要素認証検証リクエスト.
 */
public class MfaVerificationRequest {

    @NotBlank(message = "チャレンジIDは必須です")
    private String challengeId;

    @NotBlank(message = "コードは必須です")
    @Size(min = 6, max = 6, message = "コードは6桁である必要があります")
    private String code;
    
    private String deviceId;
    private String deviceInfo;
    private String ipAddress;
    private String mfaCode;
    private java.util.UUID userId;

    /**
     * デフォルトコンストラクタ.
     */
    public MfaVerificationRequest() {
    }

    /**
     * コンストラクタ.
     *
     * @param code 認証コード
     */
    public MfaVerificationRequest(String code) {
        this.code = code;
    }

    /**
     * 認証コードを取得する.
     *
     * @return 認証コード
     */
    public String getCode() {
        return code;
    }

    /**
     * 認証コードを設定する.
     *
     * @param code 認証コード
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * チャレンジIDを取得する.
     *
     * @return チャレンジID
     */
    public String getChallengeId() {
        return challengeId;
    }

    /**
     * チャレンジIDを設定する.
     *
     * @param challengeId チャレンジID
     */
    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    /**
     * デバイスIDを取得する.
     *
     * @return デバイスID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * デバイスIDを設定する.
     *
     * @param deviceId デバイスID
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * デバイス情報を取得する.
     *
     * @return デバイス情報
     */
    public String getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * デバイス情報を設定する.
     *
     * @param deviceInfo デバイス情報
     */
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    /**
     * IPアドレスを取得する.
     *
     * @return IPアドレス
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * IPアドレスを設定する.
     *
     * @param ipAddress IPアドレス
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    /**
     * MFAコードを取得する.
     *
     * @return MFAコード
     */
    public String getMfaCode() {
        return mfaCode != null ? mfaCode : code;
    }

    /**
     * MFAコードを設定する.
     *
     * @param mfaCode MFAコード
     */
    public void setMfaCode(String mfaCode) {
        this.mfaCode = mfaCode;
    }
    
    /**
     * ユーザーIDを取得する.
     *
     * @return ユーザーID
     */
    public java.util.UUID getUserId() {
        return userId;
    }

    /**
     * ユーザーIDを設定する.
     *
     * @param userId ユーザーID
     */
    public void setUserId(java.util.UUID userId) {
        this.userId = userId;
    }
}