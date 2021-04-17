package com.lakindu.bangerandcobackend.util;

public class NoSuchUserExistsException extends RuntimeException {
    public NoSuchUserExistsException(String s) {
        super(s);
    }
}
