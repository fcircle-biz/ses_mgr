package com.ses_mgr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ses_mgr.common.dto.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@org.springframework.context.annotation.Lazy
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponseDto<Void> errorResponse = ApiResponseDto.error(
                "UNAUTHORIZED",
                "認証が必要です。有効なJWTトークンを提供してください。");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}