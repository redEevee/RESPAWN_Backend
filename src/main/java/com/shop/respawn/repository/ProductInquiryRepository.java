package com.shop.respawn.repository;

import com.shop.respawn.domain.ProductInquiry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductInquiryRepository extends MongoRepository<ProductInquiry, String> {

    List<ProductInquiry> findAllByItemId(String itemId);

    List<ProductInquiry> findAllByBuyerId(String buyerId);

    List<ProductInquiry> findAllByItemIdIn(List<String> itemIds);
}
