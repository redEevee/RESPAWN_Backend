package com.shop.respawn.controller;

import com.shop.respawn.dto.OrderHistoryDto;
import com.shop.respawn.dto.OrderRefundRequestDto;
import com.shop.respawn.dto.OrderRequestDto;
import com.shop.respawn.dto.RefundRequestDetailDto;
import com.shop.respawn.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 장바구니 선택 상품 주문
     */
    @PostMapping("/cart")
    public ResponseEntity<Map<String, Object>> orderSelectedFromCart(
            @RequestBody @Valid OrderRequestDto orderRequest,
            HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            Long orderId = orderService.prepareOrderSelectedFromCart(buyerId, orderRequest);

            return ResponseEntity.ok(Map.of(
                    "message", "선택한 상품의 주문이 성공적으로 생성되었습니다.",
                    "orderId", orderId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 상품페이지에서 상품 주문
     */
    @PostMapping("/prepare")
    public ResponseEntity<Map<String, Object>> prepareOrder(
            @RequestBody OrderRequestDto orderRequest, HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);  // 로그인된 사용자 ID 가져오기

            // 주문 생성 서비스 호출 (itemId, count, buyerId 전달)
            Long orderId = orderService.createTemporaryOrder(buyerId, orderRequest.getItemId(), orderRequest.getCount());

            // 결과 응답
            return ResponseEntity.ok(Map.of("orderId", orderId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 선택된 상품 주문 완료 처리
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Map<String, Object>> completeSelectedOrder(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderRequestDto orderRequest,
            HttpSession session) {
        try {
            getBuyerIdFromSession(session);
            orderService.completeSelectedOrder(orderId, orderRequest);

            return ResponseEntity.ok(Map.of(
                    "message", "선택된 상품의 주문이 성공적으로 완료되었습니다."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 임시 주문 상세 조회 (주문 페이지용)
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetails(
            @PathVariable Long orderId,
            HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            Map<String, Object> orderDetails = orderService.getOrderDetails(orderId, buyerId);

            return ResponseEntity.ok(orderDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 로그인한 구매자의 주문 내역 조회 API
     */
    @GetMapping("/history")
    public ResponseEntity<?> getOrderHistory(HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            return ResponseEntity.ok(orderService.getOrderHistory(buyerId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 로그인한 구매자의 주문 내역 단건 조회
     */
    @GetMapping("/history/{orderId}")
    public ResponseEntity<OrderHistoryDto> getOrderDetail(@PathVariable Long orderId, HttpSession session) {
        Long buyerId = getBuyerIdFromSession(session);  // 로그인 사용자 아이디
        OrderHistoryDto order = orderService.getOrderDetail(orderId, buyerId);
        return ResponseEntity.ok(order);
    }

    /**
     * 현재 사용자의 모든 임시 주문 삭제 (TEMPORARY 상태인 주문들을 일괄 삭제)
     */
    @DeleteMapping("/temporary")
    public ResponseEntity<Map<String, Object>> deleteAllTemporaryOrders(HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            int deletedCount = orderService.deleteAllTemporaryOrders(buyerId);

            return ResponseEntity.ok(Map.of(
                    "message", "임시 주문이 성공적으로 삭제되었습니다.",
                    "deletedCount", deletedCount
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 최근 주문 조회
     */
    @GetMapping("/latest")
    public ResponseEntity<OrderHistoryDto> getLatestOrder(HttpSession session) {
        Long buyerId = getBuyerIdFromSession(session);  // 로그인 사용자 아이디

        OrderHistoryDto latestOrder = orderService.getLatestOrderByBuyerId(buyerId);

        if (latestOrder == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(latestOrder);
    }

    /**
     * 환불 가능한 목록 조회
     */
    @GetMapping("/refundable-items")
    public ResponseEntity<?> getRefundableItems(HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session); // 기존 buyerId 조회 메서드 활용
            List<OrderHistoryDto> refundableItems = orderService.getRefundableOrderItems(buyerId);
            return ResponseEntity.ok(refundableItems);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 사용자의 환불 요청
     */
    @PostMapping("/{orderId}/items/{orderItemId}/refund")
    public ResponseEntity<?> requestRefund(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @RequestBody OrderRefundRequestDto refundDto,
            HttpSession session) {

        try {
            Long buyerId = getBuyerIdFromSession(session); // 기존 세션에서 구매자 ID 조회 메서드
            orderService.requestRefund(orderId, orderItemId, buyerId, refundDto.getReason(), refundDto.getDetail());
            return ResponseEntity.ok(Map.of("message", "해당 아이템의 환불 요청이 완료되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 요청한 환불 목록 보기
     */
    @GetMapping("/refund-requests")
    public ResponseEntity<?> getRefundRequests(HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            List<OrderHistoryDto> refundRequests = orderService.getRefundRequestedItems(buyerId);
            return ResponseEntity.ok(refundRequests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 환불 요청 판매자 확인
     */
    @GetMapping("/seller/refund-requests")
    public ResponseEntity<?> getRefundRequestsOfSeller(HttpSession session) {
        try {
            Long sellerId = getSellerIdFromSession(session);
            List<RefundRequestDetailDto> refundRequests = orderService.getRefundRequestsForSeller(sellerId);
            return ResponseEntity.ok(refundRequests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 판매자 환불 요청 완료 처리
     */
    @PostMapping("/seller/refund-requests/{orderItemId}/complete")
    public ResponseEntity<?> completeRefund(
            @PathVariable Long orderItemId,
            HttpSession session) {

        try {
            Long sellerId = getSellerIdFromSession(session);
            orderService.completeRefund(orderItemId, sellerId);
            return ResponseEntity.ok(Map.of(
                    "message", "환불 요청이 성공적으로 완료되었습니다.",
                    "orderItemId", orderItemId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 판매자 환불 요청 완료 조회
     */
    @GetMapping("/seller/refund-completed")
    public ResponseEntity<?> getCompletedRefunds(HttpSession session) {
        try {
            Long sellerId = getSellerIdFromSession(session);
            List<OrderHistoryDto> completedRefunds = orderService.getCompletedRefunds(sellerId);
            return ResponseEntity.ok(completedRefunds);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 세션에서 buyerId를 가져오는 헬퍼 메서드
     */
    private Long getBuyerIdFromSession(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorities = authentication.getAuthorities().toString();
        if (authorities.equals("[ROLE_USER]")) {
            System.out.println("구매자 권한의 아이디 : " + authorities);
            return (Long) session.getAttribute("userId");
        } else throw new RuntimeException("로그인이 필요하거나 판매자 아이디 입니다.");
    }

    /**
     * 배송 완료 처리 메서드
     */
    @PostMapping("/seller/order-items/{orderItemId}/complete-delivery")
    public ResponseEntity<?> completeDelivery(
            @PathVariable Long orderItemId,
            HttpSession session) {
        try {
            Long sellerId = getSellerIdFromSession(session);
            orderService.completeDelivery(orderItemId, sellerId);
            return ResponseEntity.ok(Map.of(
                    "message", "배송이 성공적으로 완료 처리되었습니다.",
                    "orderItemId", orderItemId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
