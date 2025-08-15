package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "point_consume_link",
        indexes = {
                @Index(name = "idx_link_save_use", columnList = "save_ledger_id, use_ledger_id")
        })
public class PointConsumeLink {

    @Id @GeneratedValue
    @Column(name = "consume_link_id")
    private Long id;

    // 어느 SAVE 레코드를 얼마나 소비했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "save_ledger_id", nullable = false)
    private PointLedger saveLedger;

    // 어떤 USE(또는 EXPIRE) 레코드가 소비했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "use_ledger_id", nullable = false)
    private PointLedger useLedger;

    @Column(nullable = false)
    private Long consumedAmount;

    public static PointConsumeLink of(PointLedger save, PointLedger use, Long consumedAmount) {
        PointConsumeLink link = new PointConsumeLink();
        link.saveLedger = save;
        link.useLedger = use;
        link.consumedAmount = consumedAmount;
        return link;
    }
}
