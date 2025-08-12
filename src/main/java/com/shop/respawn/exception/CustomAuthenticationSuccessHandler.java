package com.shop.respawn.exception;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    public CustomAuthenticationSuccessHandler(BuyerRepository buyerRepository, SellerRepository sellerRepository) {
        this.buyerRepository = buyerRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();

        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer != null) {
            buyer.getAccountStatus().resetFailedLoginAttempts();
            buyerRepository.save(buyer);
        } else {
            Seller seller = sellerRepository.findByUsername(username);
            if (seller != null) {
                seller.getAccountStatus().resetFailedLoginAttempts();
                sellerRepository.save(seller);
            }
        }

        response.sendRedirect("/loginOk");
    }
}