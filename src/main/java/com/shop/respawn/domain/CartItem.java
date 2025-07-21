package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.*;

@Entity
@Table(name = "cart_item") @Getter
@NoArgsConstructor(access = PROTECTED)
public class CartItem {

    @Id @GeneratedValue
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String itemId;
    private int cartPrice;
    private int count;

    //==생성 메서드==//
    public CartItem(String itemId, int cartPrice, int count) {
        this.itemId = itemId;
        this.cartPrice = cartPrice;
        this.count = count;
    }

    //==연관관계 설정 메서드==//
    public void assignCart(Cart cart) {
        this.cart = cart;
    }

    // 정적 팩토리 메서드 (의도 강조 가능)
    public static CartItem createCartItem(Cart cart, String itemId, int cartPrice, int count) {
        CartItem cartItem = new CartItem(itemId, cartPrice, count);
        cartItem.assignCart(cart);  // 연관 관계 세팅
        return cartItem;
    }

    //==비즈니스 로직==//
    /** 장바구니 아이템 수량 변경 */
    public void changeQuantity(int newCount) {
        if (newCount <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.count = newCount;
    }

    /** 수량 증가 */
    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가량은 0보다 커야 합니다.");
        }
        this.count += amount;
    }

    /** 수량 감소 */
    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("감소량은 0보다 커야 합니다.");
        }
        if (this.count - amount <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.count -= amount;
    }

    //==조회 로직==//
    /** 주문상품 전체 가격 조회 */
    public int getTotalPrice() {
        return getCartPrice() * getCount();
    }

}
