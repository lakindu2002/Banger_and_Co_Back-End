package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/api/user")
public class UserController {
    private final UserService theUserService;

    @Autowired //dependency injection handled by entity manager
    public UserController(UserService theUserService) {
        this.theUserService = theUserService;
    }

    @GetMapping(path = "/userInformation/{username}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('CUSTOMER')")
    public ResponseEntity<UserDTO> getUserInformation(@PathVariable(name = "username", required = true) String username) throws Exception {
        User theLoggedInUser = theUserService.getUserInformation(username);
        if (theLoggedInUser == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } else {
            UserDTO theReturningDTO = new UserDTO();
            theReturningDTO.setFirstName(theLoggedInUser.getFirstName());
            theReturningDTO.setLastName(theLoggedInUser.getLastName());
            theReturningDTO.setEmailAddress(theLoggedInUser.getEmailAddress());
            theReturningDTO.setUsername(theLoggedInUser.getUsername());
            theReturningDTO.setProfilePicture(theLoggedInUser.getProfilePicture());
            theReturningDTO.setUserRole(theLoggedInUser.getUserRole().getRoleName());
            theReturningDTO.setBlackListed(theLoggedInUser.isBlackListed());
            theReturningDTO.setContactNumber(theLoggedInUser.getContactNumber());
            theReturningDTO.setDateOfBirth(theLoggedInUser.getDateOfBirth());

            return new ResponseEntity<>(theReturningDTO, HttpStatus.OK);
        }

    }
}
