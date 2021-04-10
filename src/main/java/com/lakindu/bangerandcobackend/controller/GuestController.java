package com.lakindu.bangerandcobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.service.InquiryService;
import com.lakindu.bangerandcobackend.service.UserService;
import com.lakindu.bangerandcobackend.util.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/guest") //base path of the api
//endpoint must remain unauthenticated
public class GuestController {

    private final InquiryService inquiryService;
    private final UserService userService;

    //inject the inquiry service and userService bean created due to IOC by Spring.
    public GuestController(@Autowired InquiryService inquiryService, @Autowired UserService userService) {
        this.inquiryService = inquiryService;
        this.userService = userService;
    }

    @PostMapping(path = "/createInquiry")
    public ResponseEntity<BangerAndCoResponse> createInquiry(@Valid @RequestBody Inquiry requestInquiry) {
        //inquiry object is valid

        //save the inquiry to the database
        Inquiry savedInquiry = inquiryService.saveInquiry(requestInquiry);

        //return the success message if inquiry is saved successfully
        //if exception occurs will be directed to @ExceptionHandler handling Exception
        BangerAndCoResponse response = new BangerAndCoResponse(
                String.format("Successfully Recorded Inquiry of ID: %s",
                        savedInquiry.getInquiryId()), HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK); //return the response body as JSON
        //JSON Body converted automatically by Jackson Project.
    }

    @PostMapping(
            path = "/createAccount",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BangerAndCoResponse> createAccount(
            @RequestParam(name = "userProfile") String requestUser,
            @RequestParam(name = "profilePic", required = false) MultipartFile requestProfilePic
    ) throws Exception {
        //requestUser is a Stringify of the JSON sent from the API
        //method used for the Sign Up endpoint

        //convert the Stringify JSON Object to an instance of User via Jackson Project
        ObjectMapper objectMapper = new ObjectMapper();
        User theUser = objectMapper.readValue(requestUser, User.class); //call setters

        final User createdUser = userService.createUser(theUser, requestProfilePic);

        BangerAndCoResponse response = new BangerAndCoResponse(
                String.format("account with email - %s created successfully", createdUser.getEmailAddress()),
                HttpStatus.OK.value()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/authenticate")
    public void authenticate(@Valid @RequestBody AuthRequest theAuthRequest) {

    }

    @GetMapping(path = "/getAllRentableVehicle")
    public void getAllRentableVehicles() {

    }
}
