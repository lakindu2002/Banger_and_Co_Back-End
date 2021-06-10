package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.UserUpdateDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/api/user")
public class UserController {
    private final UserService theUserService;

    @Autowired //dependency injection handled by entity manager
    public UserController(
            @Qualifier("userServiceImpl") UserService theUserService
    ) {
        this.theUserService = theUserService;
    }

    @GetMapping(path = "/userInformation/{username}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('CUSTOMER')")
    public ResponseEntity<UserDTO> getUserInformation(@PathVariable(name = "username", required = true) String username) throws Exception {
        //method is used to retrieve the user information for the user that can be viewed by the angular front end which allows updation
        UserDTO theDTO = theUserService.getUserInformation(username);
        return new ResponseEntity<>(theDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('CUSTOMER')")
    @PutMapping(path = "/update")
    public ResponseEntity<BangerAndCoResponse> updateUser(@Valid @RequestBody UserUpdateDTO theDTO) throws ResourceNotFoundException, BadValuePassedException {
        //method executed whenever the customer or the administrator wishes to update their profile.
        //if the request body is valid
        theUserService.updateUserInformation(theDTO); //call the update method
        return new ResponseEntity<>(new BangerAndCoResponse("User Updated Successfully", HttpStatus.OK.value()), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/customers/all")
    public ResponseEntity<List<UserDTO>> getAllCustomers() throws DataFormatException, IOException {
        //method called by an administrator to view all the customers registered in the system.
        List<UserDTO> customerList = theUserService.getAllCustomers(); //call service method to get customer list.
        return new ResponseEntity<>(
                customerList, HttpStatus.OK
        ); //return the response entity back to the administrator with OK (200) code to show it was a success.
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping(path = "/customer/whitelist/{username}")
    public ResponseEntity<BangerAndCoResponse> whiteListCustomer(@PathVariable(name = "username", required = true) String username) throws ResourceNotFoundException, ResourceNotUpdatedException {
        //method executed by the administrator to white list the customer so that they can make rentals again.
        //user gets blacklisted by the system automatically when they fail to collect their made rental.
        User whiteListedCustomer = theUserService.whitelistCustomer(username);
        return new ResponseEntity<>(
                new BangerAndCoResponse(String.format("%s %s Has Been Whitelisted Successfully", whiteListedCustomer.getFirstName(), whiteListedCustomer.getLastName()), HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }
}
