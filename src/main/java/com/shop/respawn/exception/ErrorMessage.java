package com.shop.respawn.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ErrorMessage {

    private String code;
    private String message;

    protected ErrorMessage() {
    }

    private ErrorMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorMessage create(String code, String message) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.code = code;
        errorMessage.message = message;
        return errorMessage;
    }
}