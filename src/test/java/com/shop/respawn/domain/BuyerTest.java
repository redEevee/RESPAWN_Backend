package com.shop.respawn.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BuyerTest {

    @Autowired
    EntityManager em;

    @Test
    @Commit
    public void addressTest() throws Exception {

        Address address1 = new Address("A", "A", "A", "A");
        Address address2 = new Address("B", "B", "B", "B");

        Buyer buyer = new Buyer("강지원", address1);
        buyer.setAddress(address2);
        em.persist(buyer);

        em.flush();
        em.clear();


    }
}