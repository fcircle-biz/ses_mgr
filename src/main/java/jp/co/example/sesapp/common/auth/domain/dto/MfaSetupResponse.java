package jp.co.example.sesapp.common.auth.domain.dto;

/**
 * 多要素認証セットアップ応答。
 * 多要素認証の設定情報を表します。
 */
public class MfaSetupResponse {
    private String secret;
    private String qrCodeUri;

    public MfaSetupResponse() {
    }

    public MfaSetupResponse(String secret, String qrCodeUri) {
        this.secret = secret;
        this.qrCodeUri = qrCodeUri;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getQrCodeUri() {
        return qrCodeUri;
    }

    public void setQrCodeUri(String qrCodeUri) {
        this.qrCodeUri = qrCodeUri;
    }
}