package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id @GeneratedValue
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = LAZY)  // ← 변경된 부분
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(unique = true, nullable = false)
    private String impUid;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String status;

    private String name;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
