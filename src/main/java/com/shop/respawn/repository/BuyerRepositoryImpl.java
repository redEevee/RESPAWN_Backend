package com.shop.respawn.repository;

import com.shop.respawn.domain.Buyer;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BuyerRepositoryImpl implements BuyerRepositoryCustom{

    private final EntityManager em;

    @Override
    public List<Buyer> findBuyerCustom() {
        return em.createQuery("select m from Buyer m", Buyer.class)
                .getResultList();
    }
}
