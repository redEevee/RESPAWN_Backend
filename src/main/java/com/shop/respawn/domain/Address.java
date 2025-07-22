package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Address {

    @Id @GeneratedValue
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    @OneToOne(mappedBy = "address",  fetch = LAZY)
    private Delivery delivery;

    private String addressName;
    private String recipient;
    private String zoneCode;
    private String baseAddress;
    private String detailAddress;
    private String phone;
    private String subPhone;
    private boolean basic;

    private Address(Buyer buyer, String addressName, String recipient, String zoneCode, String baseAddress, String detailAddress, String phone, String subPhone, boolean basic) {
        this.buyer = buyer;
        this.addressName = addressName;
        this.recipient = recipient;
        this.zoneCode = zoneCode;
        this.baseAddress = baseAddress;
        this.detailAddress = detailAddress;
        this.phone = phone;
        this.subPhone = subPhone;
        this.basic = basic;
    }

    // 정적 팩토리 메서드
    public static Address createAddress(Buyer buyer, String addressName, String recipient,
                                        String zoneCode, String baseAddress, String detailAddress,
                                        String phone, String subPhone, boolean basic) {
        Address address = new Address(buyer, addressName, recipient, zoneCode,
                baseAddress, detailAddress, phone, subPhone, basic);

        // 연관관계 편의 메서드를 통한 안전한 관계 설정
        if (buyer != null) {
            buyer.addAddress(address);
        }

        return address;
    }

    // 비즈니스 메서드로 상태 변경
    /**
     * 주소 정보를 수정하는 비즈니스 메서드
     */
    public void updateAddressInfo(String addressName, String recipient,
                                  String zoneCode, String baseAddress, String detailAddress,
                                  String phone, String subPhone) {
        this.addressName = addressName;
        this.recipient = recipient;
        this.zoneCode = zoneCode;
        this.baseAddress = baseAddress;
        this.detailAddress = detailAddress;
        this.phone = phone;
        this.subPhone = subPhone;
    }

    /**
     * 기본 주소 설정 변경
     */
    public void changeBasicStatus(boolean basic) {
        this.basic = basic;
    }

    // 내부적으로만 사용하는 세터 (패키지 레벨)
    void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

}
