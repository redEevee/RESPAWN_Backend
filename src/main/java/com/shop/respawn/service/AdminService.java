package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.user.BuyerListDto;
import com.shop.respawn.dto.user.SellerListDto;
import com.shop.respawn.dto.user.UserSummaryDto;
import com.shop.respawn.repository.AdminRepository;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    public void expireUserById(String userType, Long userId) {
        switch (userType.toLowerCase()) {
            case "buyer" -> {
                Buyer buyer = buyerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("구매자 없음: " + userId));
                if (buyer.getAccountStatus() != null) {
                    // 현재 시각보다 과거로 설정하여 만료 상태가 되게 함
                    buyer.getAccountStatus().setAccountExpiryDate(LocalDateTime.now().minusSeconds(1));
                }
            }
            case "seller" -> {
                Seller seller = sellerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("판매자 없음: " + userId));
                if (seller.getAccountStatus() != null) {
                    seller.getAccountStatus().setAccountExpiryDate(LocalDateTime.now().minusSeconds(1));
                }
            }
            default -> throw new IllegalArgumentException("userType은 buyer 또는 seller여야 합니다.");
        }
    }

    public void unexpireUserById(String userType, Long userId) {
        switch (userType.toLowerCase()) {
            case "buyer" -> {
                Buyer buyer = buyerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("구매자 없음: " + userId));
                if (buyer.getAccountStatus() != null) {
                    buyer.renewExpiryDate();
                }
            }
            case "seller" -> {
                Seller seller = sellerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("판매자 없음: " + userId));
                if (seller.getAccountStatus() != null) {
                    seller.renewExpiryDate();
                }
            }
            default -> throw new IllegalArgumentException("userType은 buyer 또는 seller여야 합니다.");
        }
    }

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
    public Page<BuyerListDto> findBuyersPaged(int page, int size, String sort, String dir, String keyword, String field) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortOrDefault(sort)));
        Page<Buyer> buyers;
        if (keyword == null || keyword.isBlank()) {
            buyers = buyerRepository.findAll(pageable);
        } else {
            buyers = switch (normalizeField(field)) {
                case "name" -> buyerRepository.findByNameContainingIgnoreCase(keyword, pageable);
                case "username" -> buyerRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
                case "email" -> buyerRepository.findByEmailContainingIgnoreCase(keyword, pageable);
                case "phoneNumber" -> buyerRepository.findByPhoneNumberContaining(keyword, pageable);
                default -> buyerRepository.findByNameContainingIgnoreCase(keyword, pageable);
            };
        }
        return buyers.map(BuyerListDto::from);
    }

    // -------- 판매자 조회 --------
    @Transactional(readOnly = true)
    public Page<SellerListDto> findSellersPaged(int page, int size, String sort, String dir, String keyword, String field) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortOrDefault(sort)));

        Page<Seller> sellers;
        if (keyword == null || keyword.isBlank()) {
            sellers = sellerRepository.findAll(pageable);
        } else {
            sellers = switch (normalizeField(field)) {
                case "name" -> sellerRepository.findByNameContainingIgnoreCase(keyword, pageable);
                case "username" -> sellerRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
                case "email" -> sellerRepository.findByEmailContainingIgnoreCase(keyword, pageable);
                case "phoneNumber" -> sellerRepository.findByPhoneNumberContaining(keyword, pageable);
                default -> sellerRepository.findByNameContainingIgnoreCase(keyword, pageable);
            };
        }
        return sellers.map(SellerListDto::from);
    }

    private String normalizeField(String field) {
        return field == null ? "name" : field.replaceAll("\\s+", "").toLowerCase();
    }

    @Transactional(readOnly = true)
    public UserSummaryDto findUserSummaryById(String userType, Long userId) {
        return switch (userType.toLowerCase()) {
            case "buyer" -> {
                Buyer buyer = buyerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("구매자 없음: " + userId));
                yield UserSummaryDto.fromBuyer(buyer);
            }
            case "seller" -> {
                Seller seller = sellerRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("판매자 없음: " + userId));
                yield UserSummaryDto.fromSeller(seller);
            }
            default -> throw new IllegalArgumentException("userType은 buyer 또는 seller여야 합니다.");
        };
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
