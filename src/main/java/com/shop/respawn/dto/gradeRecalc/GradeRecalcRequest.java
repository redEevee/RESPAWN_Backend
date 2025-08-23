package com.shop.respawn.dto.gradeRecalc;

import lombok.Data;
import java.util.List;

@Data
public class GradeRecalcRequest {
    // 비워두면 전체 구매자 대상으로 수행
    private List<Long> buyerIds;
}