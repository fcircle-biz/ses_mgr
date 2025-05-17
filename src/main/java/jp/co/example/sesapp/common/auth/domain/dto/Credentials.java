package jp.co.example.sesapp.common.auth.domain.dto;

/**
 * 認証情報。
 * ユーザーのログイン時の認証情報を表します。
 */
public class Credentials {
    private String email;
    private String password;
    private boolean rememberMe;

    public Credentials() {
    }

    public Credentials(String email, String password, boolean rememberMe) {
        this.email = email;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    /**
     * ユーザー名またはメールアドレスを取得する.
     *
     * @return ユーザー名またはメールアドレス
     */
    public String getUsernameOrEmail() {
        return email;
    }
    
    /**
     * ユーザー名またはメールアドレスを設定する.
     *
     * @param usernameOrEmail ユーザー名またはメールアドレス
     */
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.email = usernameOrEmail;
    }
}