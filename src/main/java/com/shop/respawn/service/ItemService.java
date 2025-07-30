package com.shop.respawn.service;

import com.shop.respawn.domain.Item;
import com.shop.respawn.dto.ItemDto;
import com.shop.respawn.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Item registerItem(ItemDto itemDto, Long sellerId) {

        Item newItem = new Item();
        newItem.setName(itemDto.getName());
        newItem.setDescription(itemDto.getDescription());
        newItem.setDeliveryType(itemDto.getDeliveryType());
        newItem.setDeliveryFee(itemDto.getDeliveryFee());
        newItem.setCompany(itemDto.getCompany());
        newItem.setPrice(itemDto.getPrice());
        newItem.setStockQuantity(itemDto.getStockQuantity());
        newItem.setSellerId(String.valueOf(sellerId));
        newItem.setImageUrl(itemDto.getImageUrl()); // 대표 사진 경로만 저장
        newItem.setCategoryIds(itemDto.getCategoryIds());

        return itemRepository.save(newItem); // MongoDB에 저장
    }

    public Item getItemById(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + id));
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
}
