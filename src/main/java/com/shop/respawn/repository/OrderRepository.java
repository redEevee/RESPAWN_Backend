package com.shop.respawn.repository;

import com.shop.respawn.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByBuyer_IdOrderByOrderDateDesc(Long buyerId);

}
