package com.lakindu.bangerandcobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.auth.AuthReturnBuilder;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.AuthService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public AuthController(
            @Qualifier("userServiceImpl") UserService userService,
            @Qualifier("authServiceImpl") AuthService authService,
            Validator validator) {
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
            @RequestParam(name = "profilePic", required = true) MultipartFile requestProfilePic
    ) throws Exception {
        //requestUser is a Stringify of the JSON sent from the API
        //method used for the Sign Up endpoint

        //convert the Stringify JSON Object to an instance of UserDTO via Jackson Project
        ObjectMapper objectMapper = new ObjectMapper();
        UserDTO theUserDTO = objectMapper.readValue(requestUser, UserDTO.class); //call setters

        DataBinder theDataBinder = new DataBinder(theUserDTO);
        theDataBinder.addValidators((org.springframework.validation.Validator) validator);
        theDataBinder.validate();

        final BindingResult theBindingResult = theDataBinder.getBindingResult(); //retrieve error list
        if (theBindingResult.hasErrors()) {
            //if the entity class does not meet the expected validations
            throw new ValidationException("Valid inputs were not provided for the fields during Sign Up.");
        } else {
            theUserDTO.setEmailAddress(theUserDTO.getEmailAddress().trim());
            theUserDTO.setUsername(theUserDTO.getUsername().trim());
            theUserDTO.setContactNumber(theUserDTO.getContactNumber().trim());
            theUserDTO.setEmailAddress(theUserDTO.getEmailAddress().trim());
            theUserDTO.setLastName(theUserDTO.getLastName().trim());
            theUserDTO.setFirstName(theUserDTO.getFirstName().trim());

            userService.createUser(theUserDTO, requestProfilePic);

            BangerAndCoResponse response = new BangerAndCoResponse(
                    "account created successfully",
                    HttpStatus.OK.value()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }


    @PostMapping(path = "/login")
    public ResponseEntity<HashMap<String, Object>> authenticate(@Valid @RequestBody AuthRequest theAuthRequest) throws
            Exception {
        //execute body is request is valid.
        //use auth service to perform authentication
        theAuthRequest.setUsername(theAuthRequest.getUsername().trim());
        final AuthReturnBuilder theBuiltReturn = authService.performAuthentication(theAuthRequest);

        //compile response body
        HashMap<String, Object> returnEntity = new HashMap<>();
        returnEntity.put("response", theBuiltReturn.getTheAPIResponse());
        returnEntity.put("user_info", theBuiltReturn.getUserDTO());

        //body, header, code
        return new ResponseEntity<>(returnEntity, theBuiltReturn.getReturnHeaders(), HttpStatus.OK);
    }
}
