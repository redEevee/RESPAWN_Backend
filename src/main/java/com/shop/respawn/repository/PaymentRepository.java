package com.shop.respawn.repository;

import com.shop.respawn.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByImpUid(String impUid);

    boolean existsByImpUid(String impUid);

}
