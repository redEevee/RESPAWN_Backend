package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.repository.BuyerRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MyService {

    private final BuyerRepository buyerRepository;

    private final EntityManager em;

    @Transactional
    public void initData() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Buyer buyer = new Buyer("a", "a", encoder.encode("a"), "a", "a", Role.ROLE_USER);
        buyerRepository.save(buyer);
        em.persist(buyer);

        Buyer buyer1 = new Buyer("test", "testUser", encoder.encode("testPassword"), "test@test.com", "01012345678",Role.ROLE_USER);
        buyerRepository.save(buyer1);
        em.persist(buyer1);

        em.flush();
        em.clear();
    }

}
