package com.shop.respawn.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductInquiryResponseDto {

    private String id;

    private String buyerId;

    private String buyerUsername;

    private String itemId;

    private String question;

    private String questionDetail;

    private String answer;

    private String answerDetail;

    private LocalDateTime questionDate;

    private LocalDateTime answerDate;

    private String status;

    private boolean openToPublic;

}
