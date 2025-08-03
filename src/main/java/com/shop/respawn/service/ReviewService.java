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

import static com.shop.respawn.util.MaskingUtil.maskMiddleFourChars;

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
        List<Item> sellerItems = itemService.getItemsBySellerId(sellerId);
        List<String> sellerItemIds = sellerItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Long> orderItemIds = orderItemRepository.findAllByItemIdIn(sellerItemIds).stream()
                .map(OrderItem::getId)
                .toList();

        if (orderItemIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Review> reviews = reviewRepository.findByOrderItemIdInOrderByCreatedDateDesc(
                orderItemIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList())
        );

        // 리뷰 변환을 공통 메서드로 위임 (sellerItems 전달)
        return convertReviewsToDtos(reviews, sellerItems);
    }

    // 특정 아이템(itemId)에 대한 모든 리뷰 가져오기
    public List<ReviewWithItemDto> getReviewsByItemId(String itemId) {
        List<Review> reviews = reviewRepository.findByItemIdOrderByCreatedDateDesc(itemId);

        Item item = itemService.getItemById(itemId);
        // 단일 상품이므로 리스트로 만들어 전달
        List<Item> singleItemList = List.of(item);

        // 리뷰 변환 공통 메서드 사용
        return convertReviewsToDtos(reviews, singleItemList);
    }

    public boolean existsReviewByOrderItemId(Long buyerId, String orderItemId) {
        // 본인 리뷰만 체크(옵션), buyerId 체크 생략하면 공용 체크
        return reviewRepository.findByOrderItemId(orderItemId)
                .filter(review -> review.getBuyerId().equals(String.valueOf(buyerId)))
                .isPresent();
    }

    private List<ReviewWithItemDto> convertReviewsToDtos(List<Review> reviews, List<Item> relatedItems) {
        return reviews.stream()
                .map(review -> {
                    // 리뷰 작성자 ID
                    String buyerId = review.getBuyerId();
                    String maskedUsername = "";

                    try {
                        String buyerUsername = buyerRepository.findById(Long.valueOf(buyerId))
                                .map(Buyer::getUsername)
                                .orElse("알 수 없는 사용자");
                        maskedUsername = maskMiddleFourChars(buyerUsername);
                    } catch (NumberFormatException e) {
                        maskedUsername = "알 수 없는 사용자";
                    }

                    // 리뷰의 OrderItemId로부터 itemId 확인 (OrderItem에서)
                    String orderItemId = review.getOrderItemId();
                    Item item = null;
                    try {
                        OrderItem orderItem = orderItemRepository.findById(Long.valueOf(orderItemId)).orElse(null);
                        if (orderItem != null) {
                            String itemId = orderItem.getItemId();
                            // relatedItems 중 해당 id 검색
                            item = relatedItems.stream()
                                    .filter(i -> i.getId().equals(itemId))
                                    .findFirst()
                                    .orElse(null);
                        }
                    } catch (NumberFormatException ex) {
                        // orderItemId가 숫자가 아닐 경우 예외 처리 (없으면 null 유지)
                        item = null;
                    }

                    // item이 이미 넘어온 relatedItems 단건(예: getReviewsByItemId)일 경우 처리 예외는 caller에서 조절
                    // 리뷰의 itemId와 relatedItems의 itemId가 1:1일 경우 item=null 대신 첫개 item 전달할 수도 있음

                    return new ReviewWithItemDto(review, item, maskedUsername);
                })
                .collect(Collectors.toList());
    }

}
