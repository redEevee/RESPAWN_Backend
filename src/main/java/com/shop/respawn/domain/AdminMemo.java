package com.shop.respawn.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "admin_memos")
@CompoundIndexes({
        @CompoundIndex(name = "uniq_target", def = "{'userType': 1, 'userId': 1}", unique = true)
})
public class AdminMemo {
    @Id
    private String id;

    @Indexed
    private String userType; // "buyer" | "seller"

    @Indexed
    private Long userId;     // Buyer.id 또는 Seller.id

    // 메모 내용만 저장
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
