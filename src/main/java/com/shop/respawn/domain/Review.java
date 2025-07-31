package com.shop.respawn.domain;

import jakarta.persistence.PrePersist;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    private String id;

    private String buyerId;         // 구매자 ID (MongoDB에서 구매자 ID가 String이면 String, 아니면 Long 등 맞춰서)

    private String orderItemId;     // 주문 아이템 ID (MongoDB 기반 ID)

    private int rating;             // 평점 (1~5)

    private String content;         // 리뷰 내용

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
}
