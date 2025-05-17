package jp.co.example.sesapp.config;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

/**
 * MDC（Mapped Diagnostic Context）によるログ情報の拡張フィルター.
 * リクエストごとにユニークなIDなどの診断情報をログに追加します。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String SESSION_ID = "sessionId";
    private static final String USER_ID = "userId";
    private static final String CLIENT_IP = "clientIp";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            // リクエストIDの設定
            String requestId = UUID.randomUUID().toString().replace("-", "");
            MDC.put(REQUEST_ID, requestId);
            response.setHeader("X-Request-Id", requestId);

            // セッションIDの設定
            HttpSession session = request.getSession(false);
            if (session != null) {
                MDC.put(SESSION_ID, session.getId());
            }

            // クライアントIPの設定
            String clientIp = getClientIp(request);
            MDC.put(CLIENT_IP, clientIp);

            // フィルターチェーンの続行
            filterChain.doFilter(request, response);
        } finally {
            // MDCのクリアアップ
            MDC.remove(REQUEST_ID);
            MDC.remove(SESSION_ID);
            MDC.remove(USER_ID);
            MDC.remove(CLIENT_IP);
        }
    }

    /**
     * クライアントのIPアドレスを取得します.
     * プロキシやロードバランサー経由のリクエストにも対応します。
     *
     * @param request HTTPリクエスト
     * @return クライアントのIPアドレス
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // カンマ区切りのIPアドレスリストから最初のIPを取得（プロキシチェーンの場合）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}