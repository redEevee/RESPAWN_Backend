package com.shop.respawn.exception;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import com.shop.respawn.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        switch (authentication.getAuthorities().toString()) {
            case "[ROLE_USER]" -> {
                Buyer buyer = buyerRepository.findByUsername(authentication.getName());
                if (buyer != null) {
                    buyer.getAccountStatus().resetFailedLoginAttempts();
                    buyerRepository.save(buyer);
                }
            }
            case "[ROLE_SELLER]" -> {
                Seller seller = sellerRepository.findByUsername(authentication.getName());
                if (seller != null) {
                    seller.getAccountStatus().resetFailedLoginAttempts();
                    sellerRepository.save(seller);
                }
            }
        }

        // 추가: 비밀번호 변경 필요/스누즈 여부 자동 판정
        boolean due = userService.isPasswordChangeDue(authentication.getName());   // 3개월 기준[5][2]
        boolean snoozed = userService.isSnoozed(authentication.getName());         // 7일 억제 여부[5]

        // 전달 방안 1: /loginOk 리다이렉트에 쿼리파라미터로 신호 전달
        String target = "/loginOk?pwdDue=" + due + "&pwdSnoozed=" + snoozed;

        response.sendRedirect(target);
    }
}