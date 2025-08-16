package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    public void enableUserById(String userType, Long userId) {
        switch (userType.toLowerCase()) {
            case "buyer" -> {
                Buyer buyer = buyerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("구매자 없음: " + userId));
                if (buyer.getAccountStatus() != null) {
                    buyer.getAccountStatus().setEnabled(true);
                }
            }
            case "seller" -> {
                Seller seller = sellerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("판매자 없음: " + userId));
                if (seller.getAccountStatus() != null) {
                    seller.getAccountStatus().setEnabled(true);
                }
            }
            default -> throw new IllegalArgumentException("userType은 buyer 또는 seller여야 합니다.");
        }
    }

    public void disableUserById(String userType, Long userId) {
        switch (userType.toLowerCase()) {
            case "buyer" -> {
                Buyer buyer = buyerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("구매자 없음: " + userId));
                if (buyer.getAccountStatus() != null) {
                    buyer.getAccountStatus().setEnabled(false);
                }
            }
            case "seller" -> {
                Seller seller = sellerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("판매자 없음: " + userId));
                if (seller.getAccountStatus() != null) {
                    seller.getAccountStatus().setEnabled(false);
                }
            }
            default -> throw new IllegalArgumentException("userType은 buyer 또는 seller여야 합니다.");
        }
    }

    public void unlockById(String userType, Long userId) {
        switch (userType.toLowerCase()) {
            case "buyer" -> {
                Buyer buyer = buyerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("구매자 없음: " + userId));
                if (buyer.getAccountStatus() != null) {
                    buyer.getAccountStatus().resetFailedLoginAttempts();
                }
            }
            case "seller" -> {
                Seller seller = sellerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("판매자 없음: " + userId));
                if (seller.getAccountStatus() != null) {
                    seller.getAccountStatus().resetFailedLoginAttempts();
                }
            }
            default -> throw new IllegalArgumentException("userType은 buyer 또는 seller여야 합니다.");
        }
    }

    public boolean isEnabledById(String userType, Long userId) {
        return switch (userType.toLowerCase()) {
            case "buyer" -> buyerRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("구매자 없음: " + userId))
                    .getAccountStatus().isEnabled();
            case "seller" -> sellerRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("판매자 없음: " + userId))
                    .getAccountStatus().isEnabled();
            default -> throw new IllegalArgumentException("userType은 buyer 또는 seller여야 합니다.");
        };
    }
}
