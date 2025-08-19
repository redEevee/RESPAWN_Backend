package com.shop.respawn.repository;

import com.shop.respawn.domain.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom{

    private final MongoTemplate mongoTemplate;

    private Criteria buildKeywordOrRegex(String keyword) {
        String escaped = java.util.regex.Pattern.quote(keyword);
        return new Criteria().orOperator(
                Criteria.where("name").regex(escaped, "i"),
                Criteria.where("description").regex(escaped, "i"),
                Criteria.where("company").regex(escaped, "i")
        );
    }

    @Override
    public List<Item> searchByKeywordRegex(String keyword) {
        Query q = new Query(buildKeywordOrRegex(keyword));
        return mongoTemplate.find(q, Item.class);
    }

    @Override
    public List<Item> searchByKeywordAndCategories(String keyword, List<String> categoryIds) {
        Criteria or = buildKeywordOrRegex(keyword);
        Criteria cat = Criteria.where("categoryIds").in(categoryIds);
        Query q = new Query(new Criteria().andOperator(or, cat));
        return mongoTemplate.find(q, Item.class);
    }

    @Override
    public List<Item> fullTextSearch(String keyword) {
        TextCriteria text = TextCriteria.forDefaultLanguage().matching(keyword);
        Query q = TextQuery.queryText(text).sortByScore();
        return mongoTemplate.find(q, Item.class);
    }
}
