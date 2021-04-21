package com.lakindu.bangerandcobackend.util.exceptionhandling;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String s) {
        super(s);
    }
}
