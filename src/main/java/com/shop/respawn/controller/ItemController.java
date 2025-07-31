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

    /**
     * 상품 등록
     */
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

    /**
     * Id 값으로 상품 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable String id) {
        Item item = itemService.getItemById(id);
        ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getDeliveryType(), item.getDeliveryFee(), item.getCompany(),
                item.getCompanyNumber(), item.getPrice(), item.getStockQuantity(), item.getSellerId(), item.getImageUrl(), item.getCategoryIds());
        return ResponseEntity.ok(itemDto);
    }

    /**
     * 전체 상품 조회
     */
    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        List<ItemDto> itemDtos = items.stream()
                .map(item -> new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getDeliveryType(), item.getDeliveryFee(), item.getCompany(),
                        item.getCompanyNumber(), item.getPrice(), item.getStockQuantity(), item.getSellerId(), item.getImageUrl(), item.getCategoryIds()))
                .toList();
        return ResponseEntity.ok(itemDtos);
    }

    /**
     * 자신이 등록한 아이템 조회
     */
    @GetMapping("/my-items")
    public ResponseEntity<List<ItemDto>> getItemsOfLoggedInSeller(HttpSession session) {
        Long sellerId = getSellerIdFromSession(session);  // 세션에서 로그인된 판매자 ID 조회

        List<Item> items = itemService.getItemsBySellerId(String.valueOf(sellerId));

        List<ItemDto> itemDtos = items.stream()
                .map(item -> new ItemDto(item.getId(),
                        item.getName(),
                        item.getDescription(),
                        item.getDeliveryType(),
                        item.getDeliveryFee(),
                        item.getCompany(),
                        item.getCompanyNumber(),
                        item.getPrice(),
                        item.getStockQuantity(),
                        item.getSellerId(),
                        item.getImageUrl(),
                        item.getCategoryIds()))
                .toList();

        return ResponseEntity.ok(itemDtos);
    }

    /**
     * 현재 로그인된 판매자의 ID 가져오기
     */
    private Long getSellerIdFromSession(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorities = authentication.getAuthorities().toString();
        if(authorities.equals("[ROLE_SELLER]")) {
            // 예시: 세션에 저장된 판매자 ID
            return (Long) session.getAttribute("userId");
        } else {
            throw new RuntimeException("판매자 로그인이 필요합니다.");
        }
    }
}
