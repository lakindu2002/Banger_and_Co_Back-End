package com.lakindu.bangerandcobackend.util;

public class BangerAndCoExceptionHandler {
    private final String message;
    private final String exceptionMessage;
    private final int errorCode;

    public BangerAndCoExceptionHandler(String message, String exceptionMessage, int errorCode) {
        this.message = message;
        this.exceptionMessage = exceptionMessage;
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
