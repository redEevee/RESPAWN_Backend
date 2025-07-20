package com.shop.respawn.repository;

import com.shop.respawn.domain.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String> {
}
