package com.shop.respawn.repository;

import com.shop.respawn.domain.Buyer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, Long>, BuyerRepositoryCustom {

    Buyer findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByEmail(String email);

    Optional<Buyer> findOptionalByUsername(String username);

    Buyer findByNameAndEmail(String name, String email);

    Buyer findByNameAndPhoneNumber(String name, String phoneNumber);

    Buyer findByUsernameAndNameAndEmail(String username, String name, String email);

    Buyer findByUsernameAndNameAndPhoneNumber(String username, String name, String phoneNumber);

    Page<Buyer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Buyer> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<Buyer> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<Buyer> findByPhoneNumberContaining(String phoneNumber, Pageable pageable);

    Page<Buyer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Buyer> findByNameContainingIgnoreCaseAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Buyer> findByUsernameContainingIgnoreCaseAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Buyer> findByEmailContainingIgnoreCaseAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Buyer> findByPhoneNumberContainingAndCreatedAtBetween(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
}
