package com.shop.respawn.repository;

import com.shop.respawn.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Seller findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByEmail(String email);

    Seller findByNameAndEmail(String name, String email);

    Seller findByNameAndPhoneNumber(String name, String phoneNumber);

    Seller findByUsernameAndNameAndEmail(String username, String name, String email);

    Seller findByUsernameAndNameAndPhoneNumber(String username, String name, String phoneNumber);
}
