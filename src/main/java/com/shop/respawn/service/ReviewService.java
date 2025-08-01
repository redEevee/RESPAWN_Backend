package com.shop.respawn.service;

import com.shop.respawn.domain.*;
import com.shop.respawn.dto.ReviewWithItemDto;
import com.shop.respawn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;   // MongoDB 리뷰 저장소
    private final BuyerRepository buyerRepository;     // RDBMS 구매자
    private final OrderItemRepository orderItemRepository; // RDBMS 주문 아이템
    private final ItemService itemService;

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

        // 배송 완료 여부 확인 - Order가 아닌 OrderItem의 Delivery에서 상태 확인
        Delivery delivery = orderItem.getDelivery();
        if (delivery == null || delivery.getStatus() != DeliveryStatus.DELIVERED) {
            throw new RuntimeException("배송이 완료된 주문 아이템에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 중복 리뷰 확인 (MongoDB)
        if (reviewRepository.findByOrderItemId(orderItemId).isPresent()) {
            throw new RuntimeException("이미 리뷰를 작성한 주문 아이템입니다.");
        }

        // 구매자 존재 확인
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다."));

        String itemId = orderItem.getItemId();

        // 리뷰 생성 및 저장 (MongoDB)
        Review review = Review.builder()
                .buyerId(String.valueOf(buyer.getId()))
                .orderItemId(orderItemId)
                .itemId(itemId)
                .rating(rating)
                .content(content)
                .createdDate(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
    }

    /**
     * 판매자 ID로 판매한 아이템들의 리뷰 리스트 조회
     */
    public List<ReviewWithItemDto> getReviewsBySellerId(String sellerId) {
        // 1. 판매자가 판매한 아이템 리스트 조회 (MongoDB itemId)
        List<Item> sellerItems = itemService.getItemsBySellerId(sellerId);
        List<String> sellerItemIds = sellerItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // 1-1. 해당 아이템에 대한 주문 아이템 ID 리스트 조회 (RDBMS OrderItem.id)
        List<Long> orderItemIds = orderItemRepository.findAllByItemIdIn(sellerItemIds).stream()
                .map(OrderItem::getId)
                .toList();

        if (orderItemIds.isEmpty()) {
            return Collections.emptyList(); // 조회되는 주문 아이템 없으면 빈 리스트 반환
        }

        // 2. 해당 주문 아이템 ID들로 리뷰 조회 (MongoDB)
        List<Review> reviews = reviewRepository.findByOrderItemIdIn(
                orderItemIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList())
        );

        // 3. 리뷰 + 아이템 정보를 DTO로 변환
        return reviews.stream()
                .map(review -> {
                    // 리뷰의 orderItemId 는 string 이므로 변환 필요
                    String orderItemId = review.getOrderItemId();

                    // 리뷰에 해당하는 OrderItem의 itemId 를 찾기 위해 orderItemRepository에서 조회하거나
                    // sellerItems, orderItemIds 연결이 필요함
                    // 간단히 sellerItems 에서 review의 itemId를 매칭하는 로직으로 변경

                    // orderItemRepository.findById(Long.valueOf(orderItemId)) 해도 됨
                    OrderItem orderItem = orderItemRepository.findById(Long.valueOf(orderItemId))
                            .orElse(null);

                    Item item = null;
                    if (orderItem != null) {
                        String itemId = orderItem.getItemId();
                        item = sellerItems.stream()
                                .filter(i -> i.getId().equals(itemId))
                                .findFirst()
                                .orElse(null);
                    }
                    return new ReviewWithItemDto(review, item);
                })
                .collect(Collectors.toList());
    }

    // 특정 아이템(itemId)에 대한 모든 리뷰 가져오기
    public List<ReviewWithItemDto> getReviewsByItemId(String itemId) {
        // 이제 주문아이템을 조회하지 않고 바로 리뷰를 가져옴
        List<Review> reviews = reviewRepository.findByItemId(itemId);

        // 아이템 정보 단건 조회 (프론트 출력을 위해)
        Item item = itemService.getItemById(itemId);

        // DTO 변환
        return reviews.stream()
                .map(review -> new ReviewWithItemDto(review, item))
                .toList();
    }

    public boolean existsReviewByOrderItemId(Long buyerId, String orderItemId) {
        // 본인 리뷰만 체크(옵션), buyerId 체크 생략하면 공용 체크
        return reviewRepository.findByOrderItemId(orderItemId)
                .filter(review -> review.getBuyerId().equals(String.valueOf(buyerId)))
                .isPresent();
    }

}
