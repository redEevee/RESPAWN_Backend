package com.shop.respawn.controller;

import com.shop.respawn.dto.ProductInquiryRequestDto;
import com.shop.respawn.dto.ProductInquiryResponseDto;
import com.shop.respawn.dto.ProductInquiryResponseTitlesDto;
import com.shop.respawn.service.ItemService;
import com.shop.respawn.service.ProductInquiryService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final ProductInquiryService productInquiryService;
    private final ItemService itemService;

    /**
     * 구매자 상품 문의 등록
     */
    @PostMapping
    public ResponseEntity<?> createInquiry(@RequestBody @Valid ProductInquiryRequestDto dto, HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            ProductInquiryResponseDto created = productInquiryService.createInquiry(buyerId, dto);
            return ResponseEntity.ok(Map.of("message", "상품 문의가 등록되었습니다.", "inquiry", created));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 자신이 작성한 문의 조회
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyInquiries(HttpSession session) {
        try {
            Long buyerId = getBuyerIdFromSession(session);
            List<ProductInquiryResponseDto> inquiries = productInquiryService.getInquiriesByBuyer(String.valueOf(buyerId));
            return ResponseEntity.ok(inquiries);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 전체 조회 제목만
     */
    @GetMapping("/titles")
    public ResponseEntity<?> getInquiryTitles() {
        // 상품별 혹은 전체 문의 제목 노출 (필요하면 매개변수 추가 가능)
        List<ProductInquiryResponseTitlesDto> inquiries = productInquiryService.getAllInquiryTitles();
        return ResponseEntity.ok(inquiries);
    }

    /**
     * 상품별 제목 조회
     */
    @GetMapping("/{itemId}/titles")
    public ResponseEntity<?> getInquiryTitlesByItem(@PathVariable String itemId) {
        // 특정 상품(itemId)에 대한 문의 제목만 조회
        List<ProductInquiryResponseTitlesDto> inquiries = productInquiryService.getInquiryTitlesByItemId(itemId);
        return ResponseEntity.ok(inquiries);
    }


    // 2) 문의 상세 조회: 구매자 본인 혹은 해당 상품 판매자만 접근 가능
    @GetMapping("/{inquiryId}/detail")
    public ResponseEntity<?> getInquiryDetail(@PathVariable String inquiryId, HttpSession session) {
        try {
            String userId = String.valueOf(getUserIdFromSession(session));

            ProductInquiryResponseDto inquiryDto = productInquiryService.getInquiryById(inquiryId);
            if (inquiryDto == null) {
                return ResponseEntity.notFound().build();
            }

            // 상품 판매자 ID 조회
            String itemId = inquiryDto.getItemId();
            String sellerId = itemService.getSellerIdByItemId(itemId);

            if(inquiryDto.isOpenToPublic()) {
                return ResponseEntity.ok(inquiryDto);
            } else {
                // 권한 체크: 로그인한 유저가 구매자 본인 OR 판매자면 허용
                if (userId.equals(inquiryDto.getBuyerId()) || userId.equals(sellerId)) {
                    return ResponseEntity.ok(inquiryDto);
                } else {
                    return ResponseEntity.status(403).body(Map.of("error", "권한이 없습니다."));
                }
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 상품별 문의 조회
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getInquiriesByItem(@PathVariable String itemId) {
        try {
            List<ProductInquiryResponseDto> inquiries = productInquiryService.getInquiriesByItem(itemId);
            return ResponseEntity.ok(inquiries);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 1) 판매자가 본인 상품에 대한 문의 목록 조회
    @GetMapping("/seller")
    public ResponseEntity<?> getInquiriesForSeller(HttpSession session) {
        try {
            String sellerId = String.valueOf(getSellerIdFromSession(session));
            List<ProductInquiryResponseDto> inquiries = productInquiryService.getInquiriesBySellerId(sellerId);
            return ResponseEntity.ok(inquiries);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // 2) 판매자가 문의에 답변 등록/수정
    @PostMapping("/{inquiryId}/answer")
    public ResponseEntity<?> answerInquiry(
            @PathVariable String inquiryId,
            @RequestBody Map<String, String> requestBody,
            HttpSession session) {
        try {
            String sellerId = String.valueOf(getSellerIdFromSession(session));

            String answer = requestBody.get("answer");
            String answerDetail = requestBody.get("answerDetail");
            if (answer == null || answer.isBlank() && (answerDetail == null || answerDetail.isBlank())) {
                return ResponseEntity.badRequest().body(Map.of("error", "답변 내용을 입력하세요."));
            }

            ProductInquiryResponseDto updatedInquiry = productInquiryService.answerInquiry(inquiryId, answer, answerDetail, sellerId);
            return ResponseEntity.ok(Map.of("message", "답변이 등록되었습니다.", "inquiry", updatedInquiry));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 현재 로그인된 판매자의 ID 가져오기
     */
    private Long getBuyerIdFromSession(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorities = authentication.getAuthorities().toString();
        if(authorities.equals("[ROLE_USER]")) {
            // 예시: 세션에 저장된 구매자 ID
            return (Long) session.getAttribute("userId");
        } else {
            throw new RuntimeException("구매자 로그인이 필요합니다.");
        }
    }

    /**
     * 현재 로그인된 판매자의 ID 가져오기
     */
    private Long getSellerIdFromSession(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorities = authentication.getAuthorities().toString();
        if(authorities.equals("[ROLE_SELLER]")) {
            // 예시: 세션에 저장된 구매자 ID
            return (Long) session.getAttribute("userId");
        } else {
            throw new RuntimeException("판매자 로그인이 필요합니다.");
        }
    }

    /**
     * 현재 로그인된 유저의 ID 가져오기
     */
    private Long getUserIdFromSession(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorities = authentication.getAuthorities().toString();
        if (authorities.contains("ROLE_USER") || authorities.contains("ROLE_SELLER")) {
            return (Long) session.getAttribute("userId");
        } else {
            throw new RuntimeException("로그인이 필요합니다.");
        }
    }

}
