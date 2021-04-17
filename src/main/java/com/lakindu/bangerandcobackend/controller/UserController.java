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
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('CUSTOMER')")
    @GetMapping(path = "/getUserInfo/{username}")
    public ResponseEntity<UserDTO> getUserInformation(@PathVariable(name = "username", required = true) String username) {
        final User retrievedUser = userService.getUserInfo(username);
        UserDTO returningDTO = new UserDTO();
        //construct the return data transmission object for the client.
        //done to reduce coupling between Persistence Layer and Service Layer.
        returningDTO.setFirstName(retrievedUser.getFirstName());
        returningDTO.setLastName(retrievedUser.getLastName());
        returningDTO.setEmailAddress(retrievedUser.getEmailAddress());
        returningDTO.setDateOfBirth(retrievedUser.getDateOfBirth());
        returningDTO.setContactNumber(retrievedUser.getContactNumber());
        returningDTO.setProfilePicture(retrievedUser.getProfilePicture());
        returningDTO.setBlackListed(retrievedUser.isBlackListed());
        returningDTO.setUsername(retrievedUser.getUsername());
        returningDTO.setUserRole(retrievedUser.getUserRole().getRoleName());

        return new ResponseEntity<>(returningDTO, HttpStatus.OK); //return the User DTO with user information to the client
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('CUSTOMER')")
    @PutMapping(path = "/updateUserInfo/{username}")
    public void updateUserInfo(@PathVariable(name = "username") String username) {

    }
}
