package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "buyer_id")
    private Buyer buyer; //주문 회원

    @OneToMany(mappedBy = "order", cascade = ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;

    @Enumerated(STRING)
    private OrderStatus status;

    private String pgOrderId;    // 토스페이먼츠용 주문번호

    private String orderName;      // 구매상품명 (예: "상품명 외 2건")

    private Integer totalAmount;   // 총 결제 금액

    private String paymentStatus;  // 결제 상태 (READY, SUCCESS, FAIL 등)

    //==연관관계 메서드==//
    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
        buyer.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 총 금액 계산 메서드 추가
    public int calculateTotalAmount() {
        return orderItems.stream()
                .mapToInt(item -> item.getOrderPrice() * item.getCount())
                .sum();
    }

    // 주문명 생성 메서드 추가
    public String generateOrderName() {
        if (orderItems.isEmpty()) return "상품";

        // 첫 번째 상품 정보 조회 (ItemRepository가 필요하므로 Service에서 처리)
        int itemCount = orderItems.size();
        return itemCount == 1 ? "상품 1건" : "상품 " + itemCount + "건";
    }

}
