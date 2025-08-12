package com.shop.respawn.repository;

import com.shop.respawn.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointRepository extends JpaRepository<Point, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.buyer.id = :buyerId")
    long getTotalPointsByBuyerId(@Param("buyerId") Long buyerId);

    // 전체 합계(만료 제외)
    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
            "FROM Point p " +
            "WHERE p.buyer.id = :buyerId " +
            "AND p.pointExpiryDate > CURRENT_TIMESTAMP")
    long getActiveTotalPointsByBuyerId(@Param("buyerId") Long buyerId);

}
