package com.ses_mgr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests -> 
                authorizeRequests
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    .requestMatchers("/reset-password", "/reset-password/**").permitAll()
                    .requestMatchers("/mfa-verify").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(formLogin -> 
                formLogin
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard")
                    .failureUrl("/login?error")
                    .permitAll()
            )
            .logout(logout -> 
                logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            )
            .rememberMe(rememberMe -> 
                rememberMe
                    .key("SES_MANAGEMENT_SYSTEM_REMEMBER_ME_KEY")
                    .tokenValiditySeconds(2592000) // 30日間
            )
            .sessionManagement(sessionManagement -> 
                sessionManagement
                    .maximumSessions(1)
                    .expiredUrl("/login?expired")
            );
            
        return http.build();
    }
    
    // 開発用のインメモリユーザー（本番環境では削除すること）
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("administrator")
                .password(passwordEncoder().encode("Administrator@123"))
                .roles("ADMIN")
                .build();
                
        UserDetails user = User.builder()
                .username("user12345")
                .password(passwordEncoder().encode("Password@123"))
                .roles("USER")
                .build();
                
        return new InMemoryUserDetailsManager(admin, user);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}