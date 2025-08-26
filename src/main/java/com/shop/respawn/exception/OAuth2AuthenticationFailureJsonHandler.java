package com.shop.respawn.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureJsonHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String DEFAULT_REDIRECT = "http://localhost:3000/auth/callback";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException ex) throws IOException, ServletException {

        String reason = "auth_failed"; // 기본값
        if (ex instanceof OAuth2AuthenticationException oae) {
            String code = oae.getError() != null ? oae.getError().getErrorCode() : null;
            // 백엔드-프론트 합의된 최소 코드셋으로 변환
            if ("social_id_conflict".equals(code)) {
                reason = "account_conflict"; // 프론트에서 사용자 친화적 문구로 매핑
            }
        }

        // 1) 쿼리 파라미터로 전달 (간단/권장)
        String target = UriComponentsBuilder.fromUriString(DEFAULT_REDIRECT)
                .queryParam("reason", reason) // 메시지는 프론트에서 매핑
                .build()
                .toUriString();

        // 2) 또는 쿠키로 전달 (크로스 도메인 고려 필요)
        // Cookie c = new Cookie("oauth2_reason", reason);
        // c.setPath("/");
        // c.setHttpOnly(false); // 프론트 JS에서 읽을 예정이면 false
        // c.setSecure(false);   // https 환경이면 true
        // c.setMaxAge(60);
        // c.setAttribute("SameSite", "Lax"); // Strict면 리다이렉트 직후 못 읽을 수 있음
        // response.addCookie(c);

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}