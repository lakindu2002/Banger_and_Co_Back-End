package com.lakindu.bangerandcobackend.util;

import java.util.List;

public class BangerAndCoExceptionHandler {
    private final String message;
    private final String exceptionMessage;
    private final int errorCode;
    private final List<MultipleErrorSupporter> multipleErrors;

    public BangerAndCoExceptionHandler(String message, String exceptionMessage, int errorCode, List<MultipleErrorSupporter> multipleErrors) {
        this.message = message;
        this.exceptionMessage = exceptionMessage;
        this.errorCode = errorCode;
        this.multipleErrors = multipleErrors;
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

    public List<MultipleErrorSupporter> getMultipleErrors() {
        return multipleErrors;
    }
}
