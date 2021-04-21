package com.lakindu.bangerandcobackend.util.exceptionhandling;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String s) {
        super(s);
    }
}
