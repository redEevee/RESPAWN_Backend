package com.shop.respawn.service;

import com.shop.respawn.domain.Item;
import com.shop.respawn.dto.ItemDto;
import com.shop.respawn.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Item registerItem(ItemDto itemDto) {

        Item newItem = new Item();
        newItem.setName(itemDto.getName());
        newItem.setDescription(itemDto.getDescription());
        newItem.setWireless(itemDto.getWireless());
        newItem.setPrice(itemDto.getPrice());
        newItem.setStockQuantity(itemDto.getStockQuantity());
        newItem.setSellerId(itemDto.getSellerId());
        newItem.setCategoryIds(itemDto.getCategoryIds());

        return itemRepository.save(newItem); // MongoDB에 저장
    }
}
