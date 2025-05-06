package com.ses_mgr.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 静的リソースやページアクセスの場合はフィルターをスキップ
            String path = request.getRequestURI();
            if (isPublicPath(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // APIリクエストのみJWT認証を適用
            if (path.startsWith("/api/")) {
                String jwt = getJwtFromRequest(request);
                if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromJWT(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("認証フィルター実行中のエラー: " + e.getMessage(), e);
            filterChain.doFilter(request, response);
        }
    }
    
    private boolean isPublicPath(String path) {
        // 静的リソースパスのチェック
        if (path.startsWith("/css/") || path.startsWith("/js/") || 
            path.startsWith("/images/") || path.startsWith("/webjars/") ||
            path.equals("/favicon.ico")) {
            return true;
        }
        
        // 公開ページのチェック
        String[] publicPaths = {"/login", "/", "/test", "/health", "/reset-password", "/html-test"};
        for (String publicPath : publicPaths) {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
                return true;
            }
        }
        
        // 公開APIのチェック
        if (path.startsWith("/api/v1/public/") || path.startsWith("/api/v1/test/public") ||
            path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/refresh-token") ||
            path.startsWith("/api/v1/auth/password/reset")) {
            return true;
        }
        
        return false;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}