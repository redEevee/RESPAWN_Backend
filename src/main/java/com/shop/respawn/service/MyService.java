package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.AddressDto;
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
    private final AddressService addressService;

    private final EntityManager em;

    @Transactional
    public void initData() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Buyer buyer = Buyer.createBuyer("이지은", "a", encoder.encode("a"), "iu@naver.com", "01012345678", Role.ROLE_USER);
        buyerRepository.save(buyer);
        em.persist(buyer);

        Buyer buyer1 = Buyer.createBuyer("test", "testUser", encoder.encode("testPassword"), "test@test.com", "01012345678", Role.ROLE_USER);
        buyerRepository.save(buyer1);
        em.persist(buyer1);

        Seller seller = Seller.createSeller("가나디", "b", encoder.encode("b"), "gana@naver.com", "01023456789", Role.ROLE_SELLER);
        sellerRepository.save(seller);
        em.persist(seller);

        em.flush();
        em.clear();

        AddressDto addressDto1 = new AddressDto("기본주소", "강지원", "06234", "서울시 강남구 선릉로 123", "101동 1204호", "010-9876-5432", "063-533-6832", true);
        AddressDto addressDto2 = new AddressDto("너네집", "김철수", "06234", "서울시 강남구 선릉로 123", "101동 1204호", "010-9876-5432", "063-533-6832", false);

        addressService.createAddress(1L, addressDto1);
        addressService.createAddress(1L, addressDto2);

    }

}
