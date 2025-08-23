package com.shop.respawn.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType("application/json;charset=UTF-8");
        String body = """
        {
          "timestamp":"%s",
          "status":403,
          "error":"Forbidden",
          "message":"접근 권한이 없습니다.",
          "path":"%s"
        }
        """.formatted(java.time.OffsetDateTime.now(), request.getRequestURI());
        response.getWriter().write(body);
    }
}
