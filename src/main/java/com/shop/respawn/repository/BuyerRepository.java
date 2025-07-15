package com.shop.respawn.repository;

import com.shop.respawn.domain.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuyerRepository extends JpaRepository<Buyer, Long>, BuyerRepositoryCustom {

    Buyer findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByEmail(String email);

    Buyer findByName(String name);
}
