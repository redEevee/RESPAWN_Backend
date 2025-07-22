package com.shop.respawn.controller;

import com.shop.respawn.dto.AddressDto;
import com.shop.respawn.service.AddressService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 로그인한 사용자의 주소 저장
     */
    @PostMapping("/add")
    public ResponseEntity<?> createAddress(
            @RequestBody AddressDto addressDto, HttpSession session) {
        Long buyerId = getBuyerIdFromSession(session);
        addressService.createAddress(buyerId, addressDto);
        return ResponseEntity.ok("주소 저장 성공");
    }

    /**
     * 로그인한 사용자의 모든 주소 조회 (기본 주소 우선, 최신 순)
     */
    @GetMapping
    public ResponseEntity<List<AddressDto>> getMyAddresses(HttpSession session) {
        Long buyerId = getBuyerIdFromSession(session);
        List<AddressDto> addresses = addressService.getAddressesByBuyer(buyerId);
        return ResponseEntity.ok(addresses);
    }

    /**
     * 로그인한 사용자의 기본 주소 조회
     */
    @GetMapping("/basic")
    public ResponseEntity<AddressDto> getMyBasicAddress(HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            AddressDto basicAddress = addressService.getBasicAddress(buyerId);
            return ResponseEntity.ok(basicAddress);
        } catch (IllegalStateException e) {
            // 기본 주소가 없는 경우
            return ResponseEntity.noContent().build(); // 204 No Content
        }
    }

    /**
     * 주소 정보 수정
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable Long addressId,
            @RequestBody AddressDto addressDto,
            HttpSession session) {
        Long buyerId = getBuyerIdFromSession(session);
        AddressDto updatedAddress = addressService.updateAddress(buyerId, addressId, addressDto);
        return ResponseEntity.ok(updatedAddress);
    }

    /**
     * 주소 삭제
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long addressId, HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            addressService.deleteAddress(buyerId, addressId);
            return ResponseEntity.ok("주소 삭제 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 세션에서 buyerId를 가져오는 헬퍼 메서드
     */
    private Long getBuyerIdFromSession(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorities = authentication.getAuthorities().toString();

        if(authorities.equals("[ROLE_USER]")){
            System.out.println("구매자 권한의 아이디 : " + authorities);
            return (Long) session.getAttribute("userId");
        } else throw new RuntimeException("로그인이 필요하거나 판매자 아이디 입니다.");
    }
}
