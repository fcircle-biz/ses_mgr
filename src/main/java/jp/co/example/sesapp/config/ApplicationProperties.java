package jp.co.example.sesapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * アプリケーション固有の設定プロパティを保持するクラス.
 * application.ymlの「application」セクションの設定を読み込みます。
 */
@Component
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private final Security security = new Security();
    private final Logging logging = new Logging();

    public Security getSecurity() {
        return security;
    }

    public Logging getLogging() {
        return logging;
    }

    /**
     * セキュリティ関連の設定.
     */
    public static class Security {
        private final Jwt jwt = new Jwt();

        public Jwt getJwt() {
            return jwt;
        }

        /**
         * JWT認証に関する設定.
         */
        public static class Jwt {
            private String secretKey;
            private long tokenValidityInSeconds = 86400; // デフォルト1日

            public String getSecretKey() {
                return secretKey;
            }

            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }

            public long getTokenValidityInSeconds() {
                return tokenValidityInSeconds;
            }

            public void setTokenValidityInSeconds(long tokenValidityInSeconds) {
                this.tokenValidityInSeconds = tokenValidityInSeconds;
            }
        }
    }

    /**
     * ロギング関連の設定.
     */
    public static class Logging {
        private boolean requestResponse;

        public boolean isRequestResponse() {
            return requestResponse;
        }

        public void setRequestResponse(boolean requestResponse) {
            this.requestResponse = requestResponse;
        }
    }
}