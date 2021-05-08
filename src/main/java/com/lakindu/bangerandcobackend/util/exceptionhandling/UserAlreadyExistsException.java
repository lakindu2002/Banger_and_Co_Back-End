package com.lakindu.bangerandcobackend.util.exceptionhandling;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String s) {
        super(s);
    }
}
