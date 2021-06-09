package com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions;

import org.springframework.validation.BindingResult;

/**
 * Exception thrown when the input validation triggered manually by DataBinder fails.
 */
public class InputValidNotValidatedException extends Exception {

    private final BindingResult theErrorList;


    public InputValidNotValidatedException(String s, BindingResult errorList) {
        super(s);
        this.theErrorList = errorList;
    }

    public BindingResult getTheErrorList() {
        return theErrorList;
    }
}
