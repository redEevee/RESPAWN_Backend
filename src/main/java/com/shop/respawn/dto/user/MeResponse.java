package com.shop.respawn.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeResponse {
    private boolean authenticated;
    private Boolean passwordChangeDue;
    private Boolean passwordChangeSnoozed;

    public MeResponse(boolean authenticated, Boolean passwordChangeDue, Boolean passwordChangeSnoozed) {
        this.authenticated = authenticated;
        this.passwordChangeDue = passwordChangeDue;
        this.passwordChangeSnoozed = passwordChangeSnoozed;
    }
}
