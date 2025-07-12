package com.shop.respawn.controller;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.dto.BuyerDto;
import com.shop.respawn.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @PostMapping("buyers/createNewBuyer")
    public ResponseEntity<String> createNewBuyer(@RequestBody BuyerDto buyerDto) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Buyer buyer = new Buyer(buyerDto.getName(), buyerDto.getUsername(), encoder.encode(buyerDto.getPassword()),
                buyerDto.getEmail(), buyerDto.getPhoneNumber(), "local", null, Role.ROLE_USER);

        buyerService.join(buyer);
        return ResponseEntity.ok("회원가입에 성공하였습니다.");
    }

    @GetMapping("buyers/signup/{username}")
    public Boolean checkUsernameDuplicate(@PathVariable String username) {
        return buyerService.checkUsernameDuplicate(username);
    }

}
