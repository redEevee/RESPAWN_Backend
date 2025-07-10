package com.shop.respawn.repository;

import com.shop.respawn.entity.mongodb.MainBanner;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface mainBannerRepository extends MongoRepository<MainBanner, String> {

    List<MainBanner> findByTitle(String title);

}
