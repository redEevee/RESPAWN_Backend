package com.shop.respawn.controller;


import com.shop.respawn.domain.Item;
import com.shop.respawn.dto.ItemDto;
import com.shop.respawn.service.ImageService;
import com.shop.respawn.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.bson.types.Binary;
import org.springframework.http.ResponseEntity;
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
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        // 이미지 실제 저장 (예시 - 로컬 서버에 저장)
        String imageUrl = imageService.saveImage(imageFile);

        // 이미지 URL을 DTO에 설정
        itemDto.setImageUrl(imageUrl);

        Item created = itemService.registerItem(itemDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable String id) {
        System.out.println("id = " + id);
        Item item = itemService.getItemById(id);
        ResponseEntity<Item> ok = ResponseEntity.ok(item);
        System.out.println("ok = " + ok);
        return ok;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        ResponseEntity<List<Item>> ok = ResponseEntity.ok(items);
        for (Item item : items) {
            System.out.println("item.getId() = " + item.getId());
        }
        return ok;
    }
}
