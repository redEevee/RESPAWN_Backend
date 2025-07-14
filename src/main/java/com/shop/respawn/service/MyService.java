package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.repository.BuyerRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MyService {

    private final BuyerRepository buyerRepository;

    private final EntityManager em;

    @Transactional
    public void initData() {
        Buyer buyer = new Buyer("a", "a", "a", "a", "a");
        // JPA 저장
        buyerRepository.save(buyer);
        em.persist(buyer);
        em.flush();
        em.clear();
    }

}
