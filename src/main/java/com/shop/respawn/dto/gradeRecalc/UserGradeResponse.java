package com.shop.respawn.dto.gradeRecalc;

import com.shop.respawn.domain.Grade;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserGradeResponse {
    private Long userId;
    private String username;
    private Grade tier;
}