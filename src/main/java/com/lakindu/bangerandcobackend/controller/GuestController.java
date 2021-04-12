package com.lakindu.bangerandcobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.auth.CustomUserPrincipal;
import com.lakindu.bangerandcobackend.auth.JWTHandler;
import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.entity.Inquiry;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.service.InquiryService;
import com.lakindu.bangerandcobackend.service.UserService;
import com.lakindu.bangerandcobackend.util.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/guest") //base path of the api
//endpoint must remain unauthenticated
public class GuestController {

    private final InquiryService inquiryService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTHandler theTokenIssuer;

    //inject the inquiry service and userService bean created due to IOC by Spring.
    public GuestController(@Autowired InquiryService inquiryService,
                           @Autowired UserService userService,
                           @Autowired AuthenticationManager authenticationManager,
                           @Autowired JWTHandler theTokenIssuer) {
        this.inquiryService = inquiryService;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.theTokenIssuer = theTokenIssuer;
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

    @PostMapping(path = "/login")
    public ResponseEntity<User> authenticate(@Valid @RequestBody AuthRequest theAuthRequest) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        theAuthRequest.getEmailAddress(),
                        theAuthRequest.getPassword()
                ) //spring security will authenticate the user by calling the custom UserDetailsService implementation method
                //located in the UserService
        );

        User theLoggedInUser = userService.findLoggingInUser(theAuthRequest.getEmailAddress());
        CustomUserPrincipal thePrincipal = new CustomUserPrincipal(theLoggedInUser);
        String generatedToken = theTokenIssuer.generateToken(thePrincipal);

        //return the User object with the JWT Token in the response header.
        HttpHeaders theHeaders = new HttpHeaders();
        theHeaders.add("Authorization", generatedToken);
        //allow the Authorization header to be accessed in the HTTP Response.
        theHeaders.add("Access-Control-Expose-Headers", "Authorization");


        return new ResponseEntity<>(theLoggedInUser, theHeaders, HttpStatus.OK);
    }

    @GetMapping(path = "/getAllRentableVehicle")
    public void getAllRentableVehicles() {

    }
}
