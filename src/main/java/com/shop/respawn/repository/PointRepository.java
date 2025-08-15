package com.shop.respawn.repository;

import com.shop.respawn.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.buyer.id = :buyerId")
    Long getTotalPointsByBuyerId(@Param("buyerId") Long buyerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p " +
            "WHERE p.buyer.id = :buyerId AND p.used = false AND p.pointExpiryDate > CURRENT_TIMESTAMP")
    Long getActiveTotalPointsByBuyerId(@Param("buyerId") Long buyerId);

    // 사용 가능한 포인트들을 만료일 기준 오름차순으로 조회 (FIFO)
    @Query("SELECT p FROM Point p " +
            "WHERE p.buyer.id = :buyerId AND p.used = false AND p.pointExpiryDate > CURRENT_TIMESTAMP " +
            "ORDER BY p.pointExpiryDate ASC")
    List<Point> findUsablePointsByBuyerId(@Param("buyerId") Long buyerId);

    // 포인트 사용 내역 조회
    @Query("SELECT p FROM Point p " +
            "WHERE p.buyer.id = :buyerId AND p.used = true " +
            "ORDER BY p.usedDate DESC")
    List<Point> findUsedPointsByBuyerId(@Param("buyerId") Long buyerId);

}
