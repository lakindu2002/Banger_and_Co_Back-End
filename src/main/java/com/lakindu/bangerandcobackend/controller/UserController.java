package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.service.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
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
        User theUser = theUserService.getUserInformation(username);
        if (theUser == null) {
            throw new ResourceNotFoundException("username does not exist in the system");
        } else {
            UserDTO theDTO = new UserDTO();
            theDTO.setFirstName(theUser.getFirstName());
            theDTO.setLastName(theUser.getLastName());
            theDTO.setUsername(theUser.getUsername());
            theDTO.setEmailAddress(theUser.getEmailAddress());
            theDTO.setProfilePicture(theUser.getProfilePicture());
            theDTO.setUserRole(theUser.getUserRole().getRoleName());
            theDTO.setDateOfBirth(theUser.getDateOfBirth());
            theDTO.setBlackListed(theUser.isBlackListed());
            theDTO.setContactNumber(theUser.getContactNumber());

            return new ResponseEntity<>(theDTO, HttpStatus.OK);
        }
    }
}
