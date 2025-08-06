package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.InquiryStatus;
import com.shop.respawn.domain.Item;
import com.shop.respawn.domain.ProductInquiry;
import com.shop.respawn.dto.ProductInquiryRequestDto;
import com.shop.respawn.dto.ProductInquiryResponseDto;
import com.shop.respawn.dto.ProductInquiryResponseTitlesDto;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.ProductInquiryRepository;
import com.shop.respawn.util.MaskingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductInquiryService {

    private final ProductInquiryRepository productInquiryRepository;
    private final BuyerRepository buyerRepository;
    private final ItemService itemService;

    // 문의 ID로 상세 조회
    public ProductInquiryResponseDto getInquiryById(String inquiryId) {
        ProductInquiryResponseDto productInquiryResponseDto = productInquiryRepository.findById(inquiryId)
                .map(this::toResponseDto)
                .orElse(null);
        if (productInquiryResponseDto != null) {
            String buyerId = productInquiryResponseDto.getBuyerId();
            Buyer findBuyer = buyerRepository.findById(Long.valueOf(buyerId))
                    .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다."));
            String maskUsername = MaskingUtil.maskUsername(findBuyer.getUsername());
            productInquiryResponseDto.setBuyerUsername(maskUsername);
        }
        return productInquiryResponseDto;
    }

    //상품별 제목 조회
    public List<ProductInquiryResponseTitlesDto> getInquiryTitlesByItemId(String itemId) {
        List<ProductInquiry> inquiries = productInquiryRepository.findAllByItemId(itemId);

        return inquiries.stream()
                .map(i -> {
                    ProductInquiryResponseTitlesDto dto = new ProductInquiryResponseTitlesDto();
                    dto.setId(i.getId());
                    dto.setItemId(i.getItemId());

                    String question = i.getQuestion();
                    dto.setQuestion(question.length() > 30 ? question.substring(0, 30) + "..." : question);
                    dto.setStatus(i.getStatus().name());
                    dto.setQuestionDate(i.getQuestionDate());
                    dto.setOpenToPublic(i.isOpenToPublic());

                    // 구매자 username 조회 후 마스킹 처리
                    String username = buyerRepository.findById(Long.valueOf(i.getBuyerId()))
                            .map(buyer -> MaskingUtil.maskUsername(buyer.getUsername()))
                            .orElse("알 수 없음");
                    dto.setBuyerId(i.getBuyerId());
                    dto.setBuyerUsername(username);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 제목용 제목 목록 조회 (question 일부만 잘라서 리턴하는 별도 DTO/방법 추천)
    public List<ProductInquiryResponseTitlesDto> getAllInquiryTitles() {
        List<ProductInquiry> inquiries = productInquiryRepository.findAll();
        return inquiries.stream()
                .map(i -> {
                    ProductInquiryResponseTitlesDto dto = new ProductInquiryResponseTitlesDto();
                    dto.setId(i.getId());
                    dto.setItemId(i.getItemId());
                    // 제목 역할을 하는 question의 앞부분만 보이도록 처리 (예: 30자)
                    String question = i.getQuestion();
                    dto.setQuestion(question.length() > 30 ? question.substring(0, 30) + "..." : question);
                    dto.setStatus(i.getStatus().name());
                    dto.setQuestionDate(i.getQuestionDate());
                    dto.setOpenToPublic(i.isOpenToPublic());

                    // 구매자 username 조회 후 마스킹 처리
                    String username = buyerRepository.findById(Long.valueOf(i.getBuyerId()))
                            .map(buyer -> MaskingUtil.maskUsername(buyer.getUsername()))
                            .orElse("알 수 없음");
                    dto.setBuyerId(i.getBuyerId());
                    dto.setBuyerUsername(username);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ProductInquiryResponseDto createInquiry(Long buyerId, ProductInquiryRequestDto dto) {

        ProductInquiry inquiry = new ProductInquiry();
        inquiry.setBuyerId(String.valueOf(buyerId));
        inquiry.setItemId(dto.getItemId());
        inquiry.setQuestion(dto.getQuestion());
        inquiry.setQuestionDetail(dto.getQuestionDetail());
        inquiry.setQuestionDate(LocalDateTime.now());
        inquiry.setStatus(InquiryStatus.WAITING);

        ProductInquiry saved = productInquiryRepository.save(inquiry);

        return toResponseDto(saved);
    }

    public List<ProductInquiryResponseDto> getInquiriesByBuyer(String buyerId) {
        List<ProductInquiry> inquiries = productInquiryRepository.findAllByBuyerId(buyerId);
        return inquiries.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<ProductInquiryResponseDto> getInquiriesByItem(String itemId) {
        List<ProductInquiry> inquiries = productInquiryRepository.findAllByItemId(itemId);
        return inquiries.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // 판매자의 상품에 달린 모든 문의 조회
    public List<ProductInquiryResponseDto> getInquiriesBySellerId(String sellerId) {
        // 1) 해당 판매자가 판매하는 상품 ID 리스트 조회
        List<Item> sellerItems = itemService.getItemsBySellerId(sellerId);
        List<String> sellerItemIds = sellerItems.stream()
                .map(Item::getId)
                .toList();

        // 2) 상품 ID 리스트 기준으로 문의 조회
        if (sellerItemIds.isEmpty()) {
            return List.of();
        }
        List<ProductInquiry> inquiries = productInquiryRepository.findAllByItemIdIn(sellerItemIds);

        // 3) 문의 엔티티를 DTO로 변환 후 반환
        return inquiries.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // 판매자가 문의에 답변 등록
    public ProductInquiryResponseDto answerInquiry(String inquiryId, String answer, String sellerId) {
        ProductInquiry inquiry = productInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의가 존재하지 않습니다."));

        // 1) 문의된 상품의 판매자 확인
        String itemId = inquiry.getItemId();
        String inquirySellerId = itemService.getSellerIdByItemId(itemId);

        // 2) 현재 요청한 판매자와 문의 상품의 판매자 일치 확인
        if (!sellerId.equals(inquirySellerId)) {
            throw new RuntimeException("해당 문의에 대한 답변 권한이 없습니다.");
        }

        // 3) 답변 등록 및 상태 변경
        inquiry.setAnswer(answer);
        inquiry.setAnswerDate(LocalDateTime.now());
        inquiry.setStatus(InquiryStatus.ANSWERED);

        ProductInquiry saved = productInquiryRepository.save(inquiry);
        return toResponseDto(saved);
    }


    private ProductInquiryResponseDto toResponseDto(ProductInquiry entity) {
        ProductInquiryResponseDto dto = new ProductInquiryResponseDto();
        dto.setId(entity.getId());
        dto.setBuyerId(entity.getBuyerId());
        dto.setItemId(entity.getItemId());
        dto.setQuestion(entity.getQuestion());
        dto.setQuestionDetail(entity.getQuestionDetail());
        dto.setAnswer(entity.getAnswer());
        dto.setAnswerDetail(entity.getAnswerDetail());
        dto.setQuestionDate(entity.getQuestionDate());
        dto.setAnswerDate(entity.getAnswerDate());
        dto.setStatus(entity.getStatus().name());
        dto.setOpenToPublic(entity.isOpenToPublic());
        return dto;
    }
}
