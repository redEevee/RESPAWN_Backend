package com.shop.respawn.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        String body = """
        {
          "timestamp":"%s",
          "status":401,
          "error":"Unauthorized",
          "message":"인증이 필요합니다.",
          "path":"%s"
        }
        """.formatted(java.time.OffsetDateTime.now(), request.getRequestURI());
        response.getWriter().write(body);
    }
}