package com.shop.respawn.repository;

import com.shop.respawn.domain.Item;

import java.util.List;

public interface ItemRepositoryCustom {

    List<Item> searchByKeywordRegex(String keyword);

    List<Item> searchByKeywordAndCategories(String keyword, List<String> categoryIds);

    List<Item> fullTextSearch(String keyword);
}
