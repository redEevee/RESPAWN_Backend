package com.shop.respawn.controller;

import com.shop.respawn.dto.ReviewRequestDto;
import com.shop.respawn.dto.ReviewWithItemDto;
import com.shop.respawn.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/order-items/{orderItemId}")
    public ResponseEntity<?> createReview(
            @PathVariable String orderItemId,    // MongoDB의 ID형이 String이므로 String으로 바꿈
            @RequestBody @Valid ReviewRequestDto reviewRequestDto,
            HttpSession session) {
        try {
            Long buyerId = (Long) session.getAttribute("userId");
            if (buyerId == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            reviewService.createReview(buyerId, orderItemId, reviewRequestDto.getRating(), reviewRequestDto.getContent());
            return ResponseEntity.ok("리뷰가 성공적으로 작성되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 판매자가 자신이 판매한 아이템에 대한 리뷰 보기
     */
    @GetMapping("/seller/my-reviews")
    public ResponseEntity<List<ReviewWithItemDto>> getMyItemReviews(HttpSession session) {
        try {
            String sellerId = getSellerIdFromSession(session).toString();

            System.out.println("sellerId = " + sellerId);

            List<ReviewWithItemDto> reviews = reviewService.getReviewsBySellerId(sellerId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 세션에서 sellerId를 가져오는 헬퍼 메서드
     */
    private Long getSellerIdFromSession(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorities = authentication.getAuthorities().toString();
        if (authorities.equals("[ROLE_SELLER]")) {
            System.out.println("판매자 권한의 아이디 : " + authorities);
            return (Long) session.getAttribute("userId");
        } else {
            throw new RuntimeException("판매자 로그인이 필요합니다.");
        }
    }
}