package com.lakindu.bangerandcobackend.util.exceptionhandling;

public class BangerAndCoResponse {
    //class added to handle API Success Responses to client
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
