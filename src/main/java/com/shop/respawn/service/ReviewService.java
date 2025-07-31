package com.shop.respawn.service;

import com.shop.respawn.domain.*;
import com.shop.respawn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;   // MongoDB 리뷰 저장소
    private final BuyerRepository buyerRepository;     // RDBMS 구매자
    private final OrderItemRepository orderItemRepository; // RDBMS 주문 아이템

    /**
     * 리뷰 작성
     * 배송 완료된 주문 아이템에 대해서만 MongoDB에 리뷰 저장
     */
    public void createReview(Long buyerId, String orderItemId, int rating, String content) {

        // 주문 아이템 조회 (RDBMS)
        OrderItem orderItem = orderItemRepository.findById(Long.valueOf(orderItemId))
                .orElseThrow(() -> new RuntimeException("주문 아이템을 찾을 수 없습니다."));

        // 주문 및 구매자 검증
        Order order = orderItem.getOrder();
        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("해당 주문 아이템에 대한 권한이 없습니다.");
        }

        // 배송 완료 여부 확인
        if (order.getDelivery() == null || order.getDelivery().getStatus() != DeliveryStatus.DELIVERED) {
            throw new RuntimeException("배송이 완료된 주문에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 중복 리뷰 확인 (MongoDB)
        if (reviewRepository.findByOrderItemId(orderItemId).isPresent()) {
            throw new RuntimeException("이미 리뷰를 작성한 주문 아이템입니다.");
        }

        // 구매자 존재 확인
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다."));

        // 리뷰 생성 및 저장 (MongoDB)
        Review review = Review.builder()
                .buyerId(String.valueOf(buyer.getId()))
                .orderItemId(orderItemId)
                .rating(rating)
                .content(content)
                .createdDate(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
    }
}
