package com.lakindu.bangerandcobackend.util.exceptionhandling;

import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;

@RestControllerAdvice
//combination of @ControllerAdvice and @ResponseBody (automatically uses JSON)
public class GlobalExceptionHandler {
    //this class will handle all the global exceptions thrown throughout the spring application.
    //the annotation @RestControllerAdvice enables this
    //this is the best practise for handling exceptions in Spring Boot Rest

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handlerDefaultException(Exception ex) {
        //exception handler to handle all default exceptions thrown at runtime by JVM
        String errorException = "";
        if (ex.getLocalizedMessage() == null) {
            errorException = "An Error Occurred on the Server, Please Try Again.";
        } else {
            errorException = ex.getLocalizedMessage();
        }

        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "An Error Occurred on the Server, Please Try Again.",
                errorException,
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
                "The data you sent was poorly formatted.",
                HttpStatus.BAD_REQUEST.value(),
                errorList
        );
        //return the response entity of type Bad Request back to the resource sending client
        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> resourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        //custom exception handler for a resource already existing.

        //create an exception handler object
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "This resource already exists",
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
                "Please try again after providing all the data required",
                HttpStatus.BAD_REQUEST.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleMissingPartInParameter(MissingServletRequestPartException ex) {
        //handle exceptions thrown when required Part Parameters are not provided
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "Please Provide the Files Required",
                "Please try again after uploading all the files required",
                HttpStatus.BAD_REQUEST.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleResourceNotFound(ResourceNotFoundException ex) {
        //handle exceptions thrown when the resource cannot be found in the database.
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "The Resource Could Not Be Found",
                ex.getLocalizedMessage(),
                HttpStatus.NOT_FOUND.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceCannotBeDeletedException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleResourceCannotBeDeleted(ResourceCannotBeDeletedException ex) {
        //handle exceptions thrown when the resource cannot be deleted.
        BangerAndCoExceptionHandler theHandler = new BangerAndCoExceptionHandler(
                "Resource Failed To Delete",
                ex.getLocalizedMessage(),
                HttpStatus.CONFLICT.value(),
                new ArrayList<>()
        );
        //conflict is used in cases where user is able to fix data and send to avoid conflict.
        return new ResponseEntity<>(theHandler, HttpStatus.CONFLICT); //return a conflict as operation could not be completed due to a conflict in logic.
    }

    @ExceptionHandler(BadValuePassedException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleBadValueException(BadValuePassedException ex) {
        //handle exceptions thrown when required Bad values are detected are not provided
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "Bad Input Detected",
                ex.getLocalizedMessage(),
                HttpStatus.BAD_REQUEST.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleNoAccess(AccessDeniedException ex) {
        //handle exceptions thrown when required Bad values are detected are not provided
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "Access Denied",
                "You are not allowed to view this resource.",
                HttpStatus.FORBIDDEN.value(),
                new ArrayList<>());

        return new ResponseEntity<>(exceptionHandler, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InputValidNotValidatedException.class)
    public ResponseEntity<BangerAndCoExceptionHandler> handleInputValidNotValidatedException(InputValidNotValidatedException ex) {
        //thrown when a manually triggered validation by the DataBinder fails.
        ArrayList<MultipleErrorSupporter> errorList = new ArrayList<>();
        for (ObjectError err : ex.getTheErrorList().getAllErrors()) {
            //iterate the error map for the entity and retrieve field name and the error thrown.
            errorList.add(new MultipleErrorSupporter(((FieldError) err).getField(), err.getDefaultMessage()));
        }

        //create an exception handler object
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "Please Provide Valid Inputs For the Fields",
                "The data you sent was poorly formatted.",
                HttpStatus.BAD_REQUEST.value(),
                errorList
        );
        //return the response entity of type Bad Request back to the resource sending client
        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }
}
