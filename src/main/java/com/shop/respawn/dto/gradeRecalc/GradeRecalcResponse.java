package com.shop.respawn.dto.gradeRecalc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GradeRecalcResponse {
    private int processed;      // 처리 건수
    private int succeeded;      // 성공 건수
    private int failed;         // 실패 건수
    private String message;     // 요약 메시지
}
