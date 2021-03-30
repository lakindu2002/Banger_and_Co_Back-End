package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.InquiryDTO;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.service.InquiryService;
import com.lakindu.bangerandcobackend.util.BangerAndCoExceptionHandler;
import com.lakindu.bangerandcobackend.util.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.util.MultipleErrorSupporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;

@RestController
@RequestMapping(path = "/api/guest") //base path of the api
public class GuestController {

    private final InquiryService inquiryService;

    @Autowired
    //inject the inquiry service bean created due to IOC by Spring.
    public GuestController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping(path = "/createInquiry")
    public ResponseEntity<BangerAndCoResponse> createInquiry(@Valid @RequestBody InquiryDTO requestInquiryDTO) {
        //inquiry object is valid
        Inquiry theSubmittingInquiry = new Inquiry();
        //construct the entity object to be persisted onto the database via JPA
        theSubmittingInquiry.calculateLodgedTime();
        theSubmittingInquiry.setReplied(false);
        theSubmittingInquiry.setFirstName(requestInquiryDTO.getFirstName());
        theSubmittingInquiry.setLastName(requestInquiryDTO.getLastName());
        theSubmittingInquiry.setInquirySubject(requestInquiryDTO.getInquirySubject());
        theSubmittingInquiry.setMessage(requestInquiryDTO.getMessage());
        theSubmittingInquiry.setContactNumber(requestInquiryDTO.getContactNumber());
        theSubmittingInquiry.setEmailAddress(requestInquiryDTO.getEmailAddress());

        //save the inquiry to the database
        Inquiry savedInquiry = inquiryService.saveInquiry(theSubmittingInquiry);

        //return the success message if inquiry is saved successfully
        //if exception occurs will be directed to @ExceptionHandler handling Exception
        BangerAndCoResponse response = new BangerAndCoResponse(
                String.format("Successfully Recorded Inquiry of ID: %s",
                        savedInquiry.getInquiryId()), HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK); //return the response body as JSON
        //JSON Body converted automatically by Jackson Project.
    }


    @ExceptionHandler
    public ResponseEntity<BangerAndCoExceptionHandler> handlerDefaultException(Exception ex) {
        //exception handler to handle all default exceptions thrown at runtime by JVM
        BangerAndCoExceptionHandler exceptionHandler = new BangerAndCoExceptionHandler(
                "An error occurred on our end",
                ex.getLocalizedMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null);

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
                "Please Provide Valid Inputs for The Request Body",
                ex.getLocalizedMessage(),
                HttpStatus.BAD_REQUEST.value(),
                errorList
        );
        //return the response entity of type Bad Request back to the resource sending client
        return new ResponseEntity<>(exceptionHandler, HttpStatus.BAD_REQUEST);
    }
}
