package com.shop.respawn.exception;

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

        boolean disabled = false;
        boolean locked = false;
        boolean expired = false;
        int failedAttempts = 0;

        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer != null) {
            // 1) 정지(enabled=false) 우선 체크
            if (buyer.getAccountStatus() != null && !buyer.getAccountStatus().isEnabled()) {
                disabled = true;
            } else {
                // 2) 정지가 아니라면 기존 실패 처리
                assert buyer.getAccountStatus() != null;
                buyer.getAccountStatus().increaseFailedLoginAttempts();
                failedAttempts = buyer.getAccountStatus().getFailedLoginAttempts();

                if (!buyer.getAccountStatus().isAccountNonExpired()) {
                    expired = true;
                } else if (!buyer.getAccountStatus().isAccountNonLocked()) {
                    locked = true;
                }
                buyerRepository.save(buyer);
            }
        } else {
            Seller seller = sellerRepository.findByUsername(username);
            if (seller != null) {
                // 1) 정지(enabled=false) 우선 체크
                if (seller.getAccountStatus() != null && !seller.getAccountStatus().isEnabled()) {
                    disabled = true;
                } else {
                    // 2) 정지가 아니라면 기존 실패 처리
                    assert seller.getAccountStatus() != null;
                    seller.getAccountStatus().increaseFailedLoginAttempts();
                    failedAttempts = seller.getAccountStatus().getFailedLoginAttempts();

                    // 판매자에는 만료 체크가 없는 코드였으므로 잠금만 체크
                    if (!seller.getAccountStatus().isAccountNonLocked()) {
                        locked = true;
                    }
                    sellerRepository.save(seller);
                }
            }
        }

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
}
