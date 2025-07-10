package com.shop.respawn.repository;

import com.shop.respawn.domain.Buyer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BuyerRepositoryTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private BuyerRepository buyerRepository;


    @Test
    public void testBuyer() throws Exception {
        Buyer buyer = new Buyer("buyer1");
        buyerRepository.save(buyer);

        Buyer buyer1 = buyerRepository.findById(buyer.getId()).get();
        List<Buyer> all = buyerRepository.findAll();

        System.out.println("buyer1 = " + buyer1.getName());
        for (Buyer buyer2 : all) {
            System.out.println("buyer2 = " + buyer2.getName());
        }
    }

}