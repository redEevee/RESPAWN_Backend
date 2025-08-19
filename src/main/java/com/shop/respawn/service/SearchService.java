package com.shop.respawn.service;

import com.shop.respawn.domain.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final MongoTemplate mongoTemplate;

    public List<Item> fullTextSearchSorted(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();

        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(keyword);
        Query query = TextQuery.queryText(criteria).sortByScore();
        return mongoTemplate.find(query, Item.class);
    }
}
