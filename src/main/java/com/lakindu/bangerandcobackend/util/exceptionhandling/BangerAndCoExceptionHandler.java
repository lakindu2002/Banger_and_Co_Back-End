package com.lakindu.bangerandcobackend.util.exceptionhandling;

import java.util.List;

public class BangerAndCoExceptionHandler {
    //class implemented to handle exception responses to client
    private final String message; //the high level "Resource Not found"
    private final String exceptionMessage; //actual exception  "The vehicle you are searching does not exist."
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
