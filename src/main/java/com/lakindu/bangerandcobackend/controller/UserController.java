package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.UpdateUserDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    public ResponseEntity<BangerAndCoResponse> updateUser(@Valid @RequestBody UpdateUserDTO theDTO) throws ResourceNotFoundException, BadValuePassedException {
        //method executed whenever the customer or the administrator wishes to update their profile.
        //if the request body is valid
        theUserService.updateUserInformation(theDTO); //call the update method
        return new ResponseEntity<>(new BangerAndCoResponse("User Updated Successfully", HttpStatus.OK.value()), HttpStatus.OK);
    }
}
