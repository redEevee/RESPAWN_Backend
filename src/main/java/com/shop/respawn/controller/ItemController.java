package com.shop.respawn.controller;


import com.shop.respawn.domain.Item;
import com.shop.respawn.dto.ItemDto;
import com.shop.respawn.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/register")
    public ResponseEntity<Item> registerItem(@RequestBody ItemDto itemDto) {
        Item created = itemService.registerItem(itemDto);
        return ResponseEntity.ok(created);
    }
}
