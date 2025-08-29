package com.shop.respawn.repository;

import com.shop.respawn.domain.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SellerRepository extends JpaRepository<Seller, Long>, SellerRepositoryCustom {

    Seller findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByEmail(String email);

    Seller findByNameAndEmail(String name, String email);

    Seller findByNameAndPhoneNumber(String name, String phoneNumber);

    Seller findByUsernameAndNameAndEmail(String username, String name, String email);

    Seller findByUsernameAndNameAndPhoneNumber(String username, String name, String phoneNumber);

    Page<Seller> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Seller> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<Seller> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<Seller> findByPhoneNumberContaining(String phoneNumber, Pageable pageable);

    Page<Seller> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Seller> findByNameContainingIgnoreCaseAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Seller> findByUsernameContainingIgnoreCaseAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Seller> findByEmailContainingIgnoreCaseAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Seller> findByPhoneNumberContainingAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
}
