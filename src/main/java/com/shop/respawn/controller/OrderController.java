package com.shop.respawn.controller;

import com.shop.respawn.dto.OrderRequestDto;
import com.shop.respawn.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 장바구니 전체 상품 주문
     */
    @PostMapping("/cart")
    public ResponseEntity<Map<String, Object>> orderFromCart(
            @RequestBody @Valid OrderRequestDto orderRequest,
            HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            Long orderId = orderService.orderFromCart(buyerId, orderRequest);

            return ResponseEntity.ok(Map.of(
                    "message", "주문이 성공적으로 생성되었습니다.",
                    "orderId", orderId
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 장바구니 선택 상품 주문
     */
    @PostMapping("/cart/selected")
    public ResponseEntity<Map<String, Object>> orderSelectedFromCart(
            @RequestBody @Valid OrderRequestDto orderRequest,
            HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            Long orderId = orderService.orderSelectedFromCart(buyerId, orderRequest);

            return ResponseEntity.ok(Map.of(
                    "message", "선택한 상품의 주문이 성공적으로 생성되었습니다.",
                    "orderId", orderId
            ));
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
        if(authorities.equals("[ROLE_USER]")){
            System.out.println("구매자 권한의 아이디 : " + authorities);
            return (Long) session.getAttribute("userId");
        } else throw new RuntimeException("로그인이 필요하거나 판매자 아이디 입니다.");
    }
}
