package com.lakindu.bangerandcobackend.util;

public class MultipleErrorSupporter {
    //class used in join with BangerAndCoExceptionHandler to provide composite exceptions
    private String error;
    private String message;

    public MultipleErrorSupporter(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
