package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MyService {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    private final EntityManager em;

    @Transactional
    public void initData() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Buyer buyer = new Buyer("이지은", "a", encoder.encode("a"), "iu@naver.com", "01012345678", Role.ROLE_USER_BUYER);
        buyerRepository.save(buyer);
        em.persist(buyer);
        Seller seller = new Seller("가나디", "b", encoder.encode("b"), "gana@naver.com", "01023456789", Role.ROLE_USER_SELLER);
        sellerRepository.save(seller);
        em.persist(seller);

        Buyer buyer1 = new Buyer("test", "testUser", encoder.encode("testPassword"), "test@test.com", "01012345678", Role.ROLE_USER_BUYER);
        buyerRepository.save(buyer1);
        em.persist(buyer1);

        em.flush();
        em.clear();
    }

}
