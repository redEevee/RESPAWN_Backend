package com.shop.respawn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductInquiryRequestDto {

    @NotBlank
    private String itemId;

    @NotBlank
    private String inquiryType;

    @NotBlank
    private String question;

    @NotBlank
    private String questionDetail;

    private boolean openToPublic;

}