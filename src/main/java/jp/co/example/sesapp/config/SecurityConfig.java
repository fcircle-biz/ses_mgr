package jp.co.example.sesapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Securityの設定クラス.
 * アプリケーションのセキュリティ設定を定義します。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * セキュリティフィルターチェーンの設定.
     *
     * @param http HTTPセキュリティ設定
     * @return 設定済みのセキュリティフィルターチェーン
     * @throws Exception セキュリティ設定中の例外
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // API利用のため無効化
                .authorizeHttpRequests(auth -> auth
                        // 認証不要のパス
                        .requestMatchers("/", "/login", "/error", "/api/auth/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        // アクチュエータエンドポイントは管理者のみ
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // 他のすべてのリクエストは認証が必要
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .build();
    }

    /**
     * パスワードエンコーダー.
     *
     * @return BCryptPasswordEncoderのインスタンス
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}