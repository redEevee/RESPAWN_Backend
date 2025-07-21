package com.shop.respawn.controller;

import com.shop.respawn.domain.Cart;
import com.shop.respawn.domain.Item;
import com.shop.respawn.dto.CartItemDto;
import com.shop.respawn.service.CartService;
import com.shop.respawn.service.ItemService;
import com.shop.respawn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final ItemService itemService;

    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestBody CartItemDto cartItemDto,
            HttpSession session) {


        System.out.println("cartItemDto = " + cartItemDto);
        // 세션에서 buyerId 가져오기
        Long buyerId = getBuyerIdFromSession(session);
        System.out.println("buyerId = " + buyerId);

        try {
            cartService.addItemToCart(buyerId, cartItemDto.getItemId(), cartItemDto.getCount());
            return ResponseEntity.ok("장바구니에 상품이 추가되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 장바구니 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
        Long buyerId = getBuyerIdFromSession(session);

        Cart cart = cartService.getCartByBuyerId(buyerId);
        if (cart == null) {
            return ResponseEntity.ok(Map.of(
                    "cartItems", List.of(),
                    "totalPrice", 0
            ));
        }

        // 장바구니 아이템 정보와 상품 정보를 합쳐서 반환
        List<Map<String, Object>> cartItemsWithDetails = cart.getCartItems().stream()
                .map(cartItem -> {
                    Item item = itemService.getItemById(cartItem.getItemId());
                    Map<String, Object> itemDetail = new HashMap<>();
                    itemDetail.put("cartItemId", cartItem.getId());
                    itemDetail.put("itemId", cartItem.getItemId());
                    itemDetail.put("itemName", item.getName());
                    itemDetail.put("itemDescription", item.getDescription());
                    itemDetail.put("itemPrice", item.getPrice());
                    itemDetail.put("cartPrice", cartItem.getCartPrice());
                    itemDetail.put("count", cartItem.getCount());
                    itemDetail.put("totalPrice", cartItem.getTotalPrice());
                    itemDetail.put("imageUrl", item.getImageUrl());
                    itemDetail.put("stockQuantity", item.getStockQuantity());
                    return itemDetail;
                })
                .collect(Collectors.toList());

        int totalPrice = cartService.calculateTotalPrice(buyerId);

        Map<String, Object> response = new HashMap<>();
        response.put("cartItems", cartItemsWithDetails);
        response.put("totalPrice", totalPrice);
        response.put("itemCount", cartItemsWithDetails.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    @PutMapping("/items/{cartItemId}/quantity")
    public ResponseEntity<String> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity,
            HttpSession session) {

        Long buyerId = getBuyerIdFromSession(session);

        try {
            cartService.updateCartItemQuantity(buyerId, cartItemId, quantity);
            return ResponseEntity.ok("수량이 변경되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 장바구니에서 상품 제거
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long cartItemId,
            HttpSession session) {

        Long buyerId = getBuyerIdFromSession(session);

        try {
            cartService.removeCartItem(buyerId, cartItemId);
            return ResponseEntity.ok("상품이 장바구니에서 제거되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 세션에서 buyerId를 가져오는 헬퍼 메서드
     */
    private Long getBuyerIdFromSession(HttpSession session) {
        Long buyerId = (Long) session.getAttribute("userId");
        if (buyerId == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        return buyerId;
    }
}
