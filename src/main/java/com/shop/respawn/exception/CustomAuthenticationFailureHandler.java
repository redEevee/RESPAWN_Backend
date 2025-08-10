package com.shop.respawn.exception;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    public CustomAuthenticationFailureHandler(BuyerRepository buyerRepository, SellerRepository sellerRepository) {
        this.buyerRepository = buyerRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    @Transactional
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");

        boolean locked = false;
        boolean expired = false;

        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer != null) {
            buyer.getAccountStatus().increaseFailedLoginAttempts();
            if(!buyer.getAccountStatus().isAccountNonExpired()) {
                expired =  true;
            } else if (!buyer.getAccountStatus().isAccountNonLocked()) {
                locked = true;
            }
            buyerRepository.save(buyer);
        } else {
            Seller seller = sellerRepository.findByUsername(username);
            if (seller != null) {
                seller.getAccountStatus().increaseFailedLoginAttempts();
                if (!seller.getAccountStatus().isAccountNonLocked()) {
                    locked = true;
                }
                sellerRepository.save(seller);
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        if (expired) {
            response.getWriter().write("{\"error\":\"expired\"}");
        } else if (locked) {
            // 🚨 JSON 응답으로 잠금 메시지 전송
            response.getWriter().write("{\"error\":\"locked\"}");
        } else {
            response.getWriter().write("{\"error\":\"invalid_credentials\"}");
        }
    }
}
