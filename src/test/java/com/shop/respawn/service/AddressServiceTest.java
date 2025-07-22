package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.dto.AddressDto;
import com.shop.respawn.repository.BuyerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AddressServiceTest {

    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private AddressService addressService;
    @PersistenceContext
    EntityManager em;



}