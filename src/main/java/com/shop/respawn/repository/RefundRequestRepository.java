package com.shop.respawn.repository;

import com.shop.respawn.domain.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    // orderId, buyerId 등으로 필요한 조회 메서드 추가 가능
}
