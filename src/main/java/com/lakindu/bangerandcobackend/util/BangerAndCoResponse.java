package com.lakindu.bangerandcobackend.util;

public class BangerAndCoResponse {
    private final String message;
    private final int code;

    public BangerAndCoResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
