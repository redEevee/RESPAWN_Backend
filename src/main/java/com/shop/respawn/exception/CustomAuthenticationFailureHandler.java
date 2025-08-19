package com.shop.respawn.exception;

import com.shop.respawn.domain.AccountStatus;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");

        FailureResult result = null;

        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer != null) {
            result = handleFailureForUser(buyer.getAccountStatus(), () -> buyerRepository.save(buyer));
        } else {
            Seller seller = sellerRepository.findByUsername(username);
            if (seller != null) {
                result = handleFailureForUser(seller.getAccountStatus(), () -> sellerRepository.save(seller));
            }
        }

        boolean disabled = result != null && result.disabled;
        boolean expired  = result != null && result.expired;
        boolean locked   = result != null && result.locked;
        int failedAttempts = result != null ? result.failedAttempts : 0;

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        // disabled 우선 반환
        String errorCode = disabled ? "disabled"
                : expired ? "expired"
                : locked ? "locked"
                : "invalid_credentials";

        // disabled면 실패횟수는 0으로 내려도 OK(프론트 표시 목적), 유지하고 싶으면 failedAttempts 그대로 둠
        int attemptsForResponse = disabled ? 0 : failedAttempts;

        String jsonResponse = String.format("{\"error\":\"%s\", \"failedLoginAttempts\": %d}",
                errorCode, attemptsForResponse);
        response.getWriter().write(jsonResponse);
    }

    private FailureResult handleFailureForUser(AccountStatus status, Runnable saveEntity) {
        FailureResult result = new FailureResult();
        if (status != null && !status.isEnabled()) {
            result.disabled = true;
            // disabled이면 실패횟수는 응답 표시용으로 0으로 내릴 수도 있음
            result.failedAttempts = 0;
            return result;
        }

        // 정지가 아니라면 기존 실패 처리
        assert status != null;
        status.increaseFailedLoginAttempts();
        result.failedAttempts = status.getFailedLoginAttempts();

        if (!status.isAccountNonExpired()) {
            result.expired = true;
        } else if (!status.isAccountNonLocked()) {
            result.locked = true;
        }

        // 변경사항 저장
        saveEntity.run();
        return result;
    }

    private static class FailureResult {
        boolean disabled;
        boolean locked;
        boolean expired;
        int failedAttempts;
    }
}
