package com.shop.respawn.dto.query;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FailureResultDto {
    private boolean disabled;
    private boolean expired;
    private boolean locked;
    private int failedAttempts;
}

