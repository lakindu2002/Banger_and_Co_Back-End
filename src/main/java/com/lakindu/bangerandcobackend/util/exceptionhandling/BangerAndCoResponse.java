package com.lakindu.bangerandcobackend.util.exceptionhandling;

public class BangerAndCoResponse {
    //class added to handle API Success Responses to client
    private String message;
    private int code;

    public BangerAndCoResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public BangerAndCoResponse() {
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
