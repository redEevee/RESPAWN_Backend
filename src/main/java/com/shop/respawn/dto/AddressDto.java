package com.shop.respawn.dto;

import lombok.Data;

@Data
public class AddressDto {

    private Long id;

    private String addressName;

    private String recipient;

    private String zoneCode;

    private String baseAddress;

    private String detailAddress;

    private String phone;

    private String subPhone;

    private boolean basic;

    public AddressDto(Long id, String addressName, String recipient, String zoneCode, String baseAddress, String detailAddress, String phone, String subPhone, boolean basic) {
        this.id = id;
        this.addressName = addressName;
        this.recipient = recipient;
        this.zoneCode = zoneCode;
        this.baseAddress = baseAddress;
        this.detailAddress = detailAddress;
        this.phone = phone;
        this.subPhone = subPhone;
        this.basic = basic;
    }

}
