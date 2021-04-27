package com.lakindu.bangerandcobackend.util.exceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;

@RestControllerAdvice
//combination of @ControllerAdvice and @ResponseBody
public class GlobalExceptionHandler {
    //this class will handle all the global exceptions thrown throughout the spring application.
    //the annotation @RestControllerAdvice enables this
    //this is the best practise for handling exceptions in Spring Boot Rest

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handlerDefaultException(Exception ex) {
        //exception handler to handle all default exceptions thrown at runtime by JVM
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "An Error Occurred on the Server, Please Try Again.",
                ex.getLocalizedMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
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

    @ExceptionHandler(UserAlreadyExistsException.class)
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
        //handle exceptions thrown when required Request Parameters are not provided
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "Please Provide All The Data Required",
                ex.getLocalizedMessage(),
                HttpStatus.BAD_REQUEST.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleMissingPartInParameter(MissingServletRequestPartException ex) {
        //handle exceptions thrown when required Part Parameters are not provided
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "Please Provide the Files Required",
                ex.getLocalizedMessage(),
                HttpStatus.BAD_REQUEST.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleResourceNotFound(ResourceNotFoundException ex) {
        //handle exceptions thrown when required Part Parameters are not provided
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "The Requested Data Does Not Exist",
                ex.getLocalizedMessage(),
                HttpStatus.NOT_FOUND.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.NOT_FOUND);
    }
}
