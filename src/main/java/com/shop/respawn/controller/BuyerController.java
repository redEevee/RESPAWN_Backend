package com.shop.respawn.controller;

import com.shop.respawn.dto.BuyerDto;
import com.shop.respawn.exception.ApiResponse;
import com.shop.respawn.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @PostMapping("buyers/join")
    public ResponseEntity<ApiResponse<?>> Join(@RequestBody BuyerDto buyerDto) {
        buyerService.join(buyerDto);
        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }

    @GetMapping("buyers/signup/username/{username}")
    public Boolean checkUsernameDuplicate(@PathVariable String username) {
        return buyerService.checkUsernameDuplicate(username);
    }

    @GetMapping("buyers/signup/phoneNumber/{phoneNumber}")
    public Boolean checkPhoneNumberDuplicate(@PathVariable String phoneNumber) {
        return buyerService.checkPhoneNumberDuplicate(phoneNumber);
    }

    @GetMapping("buyers/signup/email/{email}")
    public Boolean checkEmailDuplicate(@PathVariable String email) {
        return buyerService.checkEmailDuplicate(email);
    }

}
