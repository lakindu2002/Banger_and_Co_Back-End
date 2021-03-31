package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.service.InquiryService;
import com.lakindu.bangerandcobackend.util.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/guest") //base path of the api
//endpoint must remain unauthenticated
public class GuestController {

    private final InquiryService inquiryService;

    @Autowired
    //inject the inquiry service bean created due to IOC by Spring.
    public GuestController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping(path = "/createInquiry")
    public ResponseEntity<BangerAndCoResponse> createInquiry(@Valid @RequestBody Inquiry requestInquiry) {
        //inquiry object is valid
        Inquiry theSubmittingInquiry = new Inquiry();
        //construct the entity object to be persisted onto the database via JPA
        theSubmittingInquiry.calculateLodgedTime();
        theSubmittingInquiry.setReplied(false);
        theSubmittingInquiry.setFirstName(requestInquiry.getFirstName());
        theSubmittingInquiry.setLastName(requestInquiry.getLastName());
        theSubmittingInquiry.setInquirySubject(requestInquiry.getInquirySubject());
        theSubmittingInquiry.setMessage(requestInquiry.getMessage());
        theSubmittingInquiry.setContactNumber(requestInquiry.getContactNumber());
        theSubmittingInquiry.setEmailAddress(requestInquiry.getEmailAddress());

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

    @PostMapping(path = "/createAccount")
    public void createAccount() {

    }

    @PostMapping(path = "/authenticate")
    public void authenticate() {

    }

    @GetMapping(path = "/getAllRentableVehicle")
    public void getAllRentableVehicles() {

    }
}
