package com.shop.respawn.repository;

import com.shop.respawn.domain.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<Buyer, Long>, BuyerRepositoryCustom {
}
