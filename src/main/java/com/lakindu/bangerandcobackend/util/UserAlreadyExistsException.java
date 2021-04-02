package com.lakindu.bangerandcobackend.util;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String s) {
        super(s);
    }
}
