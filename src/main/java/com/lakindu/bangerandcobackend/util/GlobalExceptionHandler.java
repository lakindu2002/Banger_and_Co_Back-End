package com.lakindu.bangerandcobackend.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;

@RestControllerAdvice
//combination of @ControllerAdvice and @ResponseBody
public class GlobalExceptionHandler {
    //this class will handle all the global exceptions thrown throughout the spring application.
    //the annotation @RestControllerAdvice enables this
    //this is the best practise for handling exceptions in Spring Boot Rest

    @ExceptionHandler
    public ResponseEntity<BangerAndCoExceptionHandler> handlerDefaultException(Exception ex) {
        //exception handler to handle all default exceptions thrown at runtime by JVM
        System.out.println(ex);
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "An error occurred",
                ex.getLocalizedMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<BangerAndCoExceptionHandler> methodArgumentNotValid(MethodArgumentNotValidException ex) {
        //custom exception handler for JPA Bean Validation Errors.
        ArrayList<MultipleErrorSupporter> errorList = new ArrayList<>();
        for (ObjectError err : ex.getBindingResult().getAllErrors()) {
            //iterate the error map for the entity and retrieve field name and the error thrown.
            errorList.add(new MultipleErrorSupporter(((FieldError) err).getField(), err.getDefaultMessage()));
        }

        //create an exception handler object
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "Please Provide Valid Inputs For the Fields",
                ex.getLocalizedMessage(),
                HttpStatus.BAD_REQUEST.value(),
                errorList
        );
        //return the response entity of type Bad Request back to the resource sending client
        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<BangerAndCoExceptionHandler> userAlreadyExistsException(UserAlreadyExistsException ex) {
        //custom exception handler for User Already Exists.

        //create an exception handler object
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "Your Account Not Been Created Successfully",
                ex.getLocalizedMessage(),
                HttpStatus.CONFLICT.value(),
                new ArrayList<>()
        );
        //return the response entity of type Conflict back to the resource sending client
        return new ResponseEntity<>(exceptionHandler, HttpStatus.CONFLICT);
    }
}
