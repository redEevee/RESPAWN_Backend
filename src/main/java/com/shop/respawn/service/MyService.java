package com.shop.respawn.service;

import com.shop.respawn.domain.*;
import com.shop.respawn.dto.AddressDto;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        Buyer buyer = Buyer.createBuyerWithInitLists("이지은", "a", encoder.encode("a"), "iu@naver.com", "01012345678", Role.ROLE_USER);
        buyerRepository.save(buyer);
        em.persist(buyer);

        Buyer buyer1 = Buyer.createBuyerWithInitLists("test", "testUser", encoder.encode("testPassword"), "test@test.com", "01012345678", Role.ROLE_USER);
        buyerRepository.save(buyer1);
        em.persist(buyer1);

        Seller seller = Seller.createSeller("가나디", "b", encoder.encode("b"), "gana@naver.com", "01023456789", Role.ROLE_SELLER);
        sellerRepository.save(seller);
        em.persist(seller);

        em.flush();
        em.clear();

        AddressDto addressDto1 = new AddressDto(1L, "기본주소", "강지원", "06234", "서울시 강남구 선릉로 123", "101동 1204호", "010-9876-5432", "063-533-6832", true);
        AddressDto addressDto2 = new AddressDto(2L, "너네집", "김철수", "06234", "서울시 강남구 선릉로 123", "101동 1204호", "010-9876-5432", "063-533-6832", false);

        addressService.createAddress(1L, addressDto1);
        addressService.createAddress(1L, addressDto2);

        // 주문 데이터 생성 예시
        Item item1 = new Item();
        item1.setId("68822f86e8223dd3d36c5db5");
        Item item2 = new Item();
        item2.setId("68823033e8223dd3d36c5db6");
        Item item3 = new Item();
        item3.setId("6882312ce8223dd3d36c5db8");

        // 날짜 예시 (오늘부터 6일 전까지)
        LocalDateTime baseDate = LocalDateTime.now();

        Address defaultAddress = em.find(Address.class, 1L);

        for (int i = 0; i < 6; i++) {
            LocalDateTime orderDate = baseDate.minusDays(i);

            Delivery delivery = new Delivery();
            delivery.setStatus(DeliveryStatus.READY);
            delivery.setAddress(defaultAddress);

            Order order = new Order();
            order.setBuyer(buyer);
            order.setDelivery(delivery);
            order.setOrderDate(orderDate);
            order.setStatus(OrderStatus.ORDERED);

            OrderItem oi1 = OrderItem.createOrderItem(item1, 30, 1);
            order.addOrderItem(oi1);

            if (i % 3 == 0) {
                OrderItem oi2 = OrderItem.createOrderItem(item2, 20, 2);
                order.addOrderItem(oi2);
            }

            int totalAmount = order.calculateTotalAmount();
            order.setTotalAmount(totalAmount);

            order.setOrderName(order.generateOrderName());
            order.setPgOrderId("ORDER_" + System.currentTimeMillis() + "_" + i);
            order.setPaymentStatus("READY");

            em.persist(delivery);
            em.persist(order);
        }

    }

}
