package com.lakindu.bangerandcobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.util.authutils.AuthReturnBuilder;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.service.AuthService;
import com.lakindu.bangerandcobackend.service.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashMap;

@RestController
@RequestMapping(path = "/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final Validator validator;

    @Autowired
    public AuthController(UserService userService, AuthService authService, Validator validator) {
        this.userService = userService;
        this.authService = authService;
        this.validator = validator;
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

        DataBinder theDataBinder = new DataBinder(theUser);
        theDataBinder.addValidators((org.springframework.validation.Validator) validator);
        theDataBinder.validate();

        final BindingResult theBindingResult = theDataBinder.getBindingResult(); //retrieve error list
        if (theBindingResult.hasErrors()) {
            //if the entity class does not meet the expected validations
            throw new ValidationException("Valid inputs were not provided for the fields during Sign Up.");
        } else {
            theUser.setEmailAddress(theUser.getEmailAddress().trim());
            theUser.setUsername(theUser.getUsername().trim());
            theUser.setContactNumber(theUser.getContactNumber().trim());
            theUser.setEmailAddress(theUser.getEmailAddress().trim());
            theUser.setLastName(theUser.getLastName().trim());

            final User createdUser = userService.createUser(theUser, requestProfilePic);
            BangerAndCoResponse response = new BangerAndCoResponse(
                    String.format("account with username - %s created successfully", createdUser.getUsername()),
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @PostMapping(path = "/login")
    public ResponseEntity<HashMap<String, Object>> authenticate(@Valid @RequestBody AuthRequest theAuthRequest) throws Exception {
        //execute body is request is valid.
        //use auth service to perform authentication
        final AuthReturnBuilder theBuiltReturn = authService.performAuthentication(theAuthRequest);
        theAuthRequest.setUsername(theAuthRequest.getUsername().trim());

        //compile response body
        HashMap<String, Object> returnEntity = new HashMap<>();
        returnEntity.put("response", theBuiltReturn.getTheAPIResponse());
        returnEntity.put("user_info", theBuiltReturn.getUserDTO());

        return new ResponseEntity<>(returnEntity, theBuiltReturn.getReturnHeaders(), HttpStatus.OK);
    }
}
