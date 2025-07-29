package com.shop.respawn.repository;

import com.shop.respawn.domain.Order;
import com.shop.respawn.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByBuyer_IdOrderByOrderDateDesc(Long buyerId);

    Order findTop1ByBuyer_IdAndStatusOrderByOrderDateDesc(Long buyerId, OrderStatus status);

    List<Order> findByBuyerIdAndStatus(Long buyerId, OrderStatus status);

    /**
     * 구매자 ID와 주문 상태 목록, 결제 상태로 주문 조회
     */
    List<Order> findByBuyerIdAndStatusInAndPaymentStatus(Long buyerId, List<OrderStatus> statuses, String paymentStatus);

    /**
     * 구매자 ID와 특정 상태로 주문 조회 (최신순)
     */
    List<Order> findByBuyer_IdAndStatusOrderByOrderDateDesc(Long buyerId, OrderStatus status);

    /**
     * 구매자 ID와 주문 상태 목록으로 주문 조회
     */
    List<Order> findByBuyerIdAndStatusIn(Long buyerId, List<OrderStatus> statuses);
}
