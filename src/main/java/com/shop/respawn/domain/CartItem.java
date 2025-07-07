package com.shop.respawn.domain;

import com.shop.respawn.domain.item.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "cart_item")
@Getter @Setter
public class CartItem {

    @Id @GeneratedValue
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int cartPrice;
    private int count;

    //==생성 메서드==//
    public static CartItem createCartItem(Item item, int cartPrice, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCartPrice(cartPrice);
        cartItem.setCount(count);
        return cartItem;
    }

    //==조회 로직==//
    /** 주문상품 전체 가격 조회 */
    public int getTotalPrice() {
        return getCartPrice() * getCount();
    }

}
