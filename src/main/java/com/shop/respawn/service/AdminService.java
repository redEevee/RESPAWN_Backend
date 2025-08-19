package com.shop.respawn.service;

import com.shop.respawn.domain.Admin;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.user.UserSummaryDto;
import com.shop.respawn.repository.AdminRepository;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;

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

    // -------- 구매자 조회 --------
    @Transactional(readOnly = true)
    public List<UserSummaryDto> findAllBuyers() {
        return buyerRepository.findAll().stream()
                .map(UserSummaryDto::fromBuyer)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryDto> findBuyersPaged(int page, int size, String sort, String dir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortOrDefault(sort)));
        Page<Buyer> buyers = buyerRepository.findAll(pageable);
        return buyers.map(UserSummaryDto::fromBuyer);
    }

    // -------- 판매자 조회 --------
    @Transactional(readOnly = true)
    public List<UserSummaryDto> findAllSellers() {
        return sellerRepository.findAll().stream()
                .map(UserSummaryDto::fromSeller)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryDto> findSellersPaged(int page, int size, String sort, String dir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortOrDefault(sort)));
        return sellerRepository.findAll(pageable).map(UserSummaryDto::fromSeller);
    }

    private String sortOrDefault(String sort) {
        // 지원 정렬 필드 화이트리스트: username, name, id
        if (sort == null || sort.isBlank()) return "username";
        return switch (sort.toLowerCase()) {
            case "username", "name", "id" -> sort;
            default -> "username";
        };
    }
}
