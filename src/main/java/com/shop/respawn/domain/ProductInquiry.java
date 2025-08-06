package com.shop.respawn.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "product_inquiry")
public class ProductInquiry {

    @Id
    private String id;  // MongoDB는 일반적으로 String _id 사용

    @Indexed
    private String buyerId;  // 구매자 ID (String)

    @Indexed
    private String itemId;   // 문의 대상 상품 ID

    private String question;

    private String questionDetail;

    private String answer;

    private String answerDetail;

    private LocalDateTime questionDate;

    private LocalDateTime answerDate;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status = InquiryStatus.WAITING;

    private boolean openToPublic = true;  // 기본 공개

}
