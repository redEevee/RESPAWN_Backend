package com.shop.respawn.controller;


import com.shop.respawn.domain.Item;
import com.shop.respawn.dto.ItemDto;
import com.shop.respawn.service.ImageService;
import com.shop.respawn.service.ItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final ImageService imageService;

    @PostMapping("/register")
    public ResponseEntity<?> registerItem(
            @RequestPart("itemDto") ItemDto itemDto,
            @RequestPart("image") MultipartFile imageFile,
            HttpSession session) throws IOException {

        Long sellerId = getSellerIdFromSession(session); // 판매자 ID 가져오기

        // 이미지 실제 저장 (예시 - 로컬 서버에 저장)
        String imageUrl = imageService.saveImage(imageFile);

        // 이미지 URL을 DTO에 설정
        itemDto.setImageUrl(imageUrl);

        Item created = itemService.registerItem(itemDto, sellerId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable String id) {
        Item item = itemService.getItemById(id);
        ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getDeliveryType(), item.getDeliveryFee(), item.getCompany(),
                item.getCompanyNumber(), item.getPrice(), item.getStockQuantity(), item.getSellerId(), item.getImageUrl(), item.getCategoryIds());
        return ResponseEntity.ok(itemDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        List<ItemDto> itemDtos = items.stream()
                .map(item -> new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getDeliveryType(), item.getDeliveryFee(), item.getCompany(),
                        item.getCompanyNumber(), item.getPrice(), item.getStockQuantity(), item.getSellerId(), item.getImageUrl(), item.getCategoryIds()))
                .toList();
        return ResponseEntity.ok(itemDtos);
    }

    private Long getSellerIdFromSession(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorities = authentication.getAuthorities().toString();
        if(authorities.equals("[ROLE_SELLER]")) {
            // 예시: 세션에 저장된 판매자 ID
            return (Long) session.getAttribute("sellerId");
        } else {
            throw new RuntimeException("판매자 로그인이 필요합니다.");
        }
    }
}
