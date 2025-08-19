package com.shop.respawn.repository;

import com.shop.respawn.domain.Item;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ItemRepository extends MongoRepository<Item, String> {
    List<Item> findBySellerId(String sellerId);

    // 1) 단순 부분 일치(정규식, 대소문자 무시)
    @Query("{ $or: [ " +
            "  { 'name':        { $regex: ?0, $options: 'i' } }, " +
            "  { 'description': { $regex: ?0, $options: 'i' } }, " +
            "  { 'company':     { $regex: ?0, $options: 'i' } } " +
            "] }")
    List<Item> searchByKeywordRegex(String keyword);

    // 2) 카테고리+키워드(선택사항)
    @Query("{ $and: [ " +
            "  { $or: [ " +
            "      { 'name':        { $regex: ?0, $options: 'i' } }, " +
            "      { 'description': { $regex: ?0, $options: 'i' } }, " +
            "      { 'company':     { $regex: ?0, $options: 'i' } } " +
            "  ] }, " +
            "  { 'categoryIds': { $in: ?1 } } " +
            "] }")
    List<Item> searchByKeywordAndCategories(String keyword, List<String> categoryIds);

    // 3) 텍스트 검색(텍스트 인덱스 사용 시)
    @Query("{ $text: { $search: ?0 } }")
    List<Item> fullTextSearch(String keyword);
}
