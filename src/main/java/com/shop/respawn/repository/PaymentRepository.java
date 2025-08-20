package com.shop.respawn.repository;

import com.shop.respawn.domain.Order;
import com.shop.respawn.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    @Query("select coalesce(sum(p.amount),0) " +
            "from Payment p " +
            "where p.buyer.id = :buyerId " +
            "and p.status = 'paid' " +
            "and p.createdAt between :start and :end")
    Long sumMonthlyAmountByBuyer(Long buyerId, LocalDateTime start, LocalDateTime end);
}
