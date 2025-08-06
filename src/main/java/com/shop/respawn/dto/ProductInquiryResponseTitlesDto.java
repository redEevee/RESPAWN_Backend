package com.shop.respawn.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductInquiryResponseTitlesDto {

    private String id;

    private String buyerId;

    private String buyerUsername;

    private String itemId;

    private String inquiryType;

    private String question;

    private LocalDateTime questionDate;

    private String status;

    private boolean openToPublic;

}
