package com.shop.respawn.service;

import com.shop.respawn.domain.Item;
import com.shop.respawn.domain.ItemStatus;
import com.shop.respawn.dto.ItemDto;
import com.shop.respawn.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;

    public Item registerItem(ItemDto itemDto, Long sellerId) {
        try {
            Item newItem = new Item();
            newItem.setName(itemDto.getName());
            newItem.setDeliveryType(itemDto.getDeliveryType());
            newItem.setDeliveryFee(itemDto.getDeliveryFee());
            newItem.setCompany(itemDto.getCompany());
            newItem.setCompanyNumber(itemDto.getCompanyNumber());
            newItem.setPrice(itemDto.getPrice());
            newItem.setStockQuantity(itemDto.getStockQuantity());
            newItem.setSellerId(String.valueOf(sellerId));
            newItem.setImageUrl(itemDto.getImageUrl()); // 대표 사진 경로만 저장
            newItem.setCategoryIds(itemDto.getCategoryIds());
            newItem.setDescription(itemDto.getDescription());
            if (newItem.getStatus() == null && ItemStatus.class.isEnum()) {
                newItem.setStatus(ItemStatus.SALE);
            }
            return itemRepository.save(newItem); // MongoDB에 저장
        } catch (Exception e) {
            System.err.println("상품 등록 실패: " + e.getMessage());
            throw new RuntimeException("상품 등록에 실패했습니다. [상세원인: " + e.getMessage() + "]", e);
        }
    }

    public Item getItemById(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + id));
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> getItemsBySellerId(String sellerId) {
        return itemRepository.findBySellerId(sellerId);
    }

    /**
     * 상품의 판매상태 조작 메서드
     */
    public void changeItemStatus(String itemId, Long sellerId, ItemStatus status) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        if (!item.getSellerId().equals(String.valueOf(sellerId))) {
            throw new RuntimeException("본인이 등록한 상품만 상태를 변경할 수 있습니다.");
        }
        item.setStatus(status);
        itemRepository.save(item);
    }
}
