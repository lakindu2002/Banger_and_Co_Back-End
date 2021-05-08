package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.UpdateUserDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
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
        User theUser = theUserService.getUserInformationWithImageDecompression(username);

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

    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('CUSTOMER')")
    @PutMapping(path = "/update")
    public ResponseEntity<BangerAndCoResponse> updateUser(@Valid @RequestBody UpdateUserDTO theDTO) throws ResourceNotFoundException {
        //if the request body is valid
        final User userInfo = theUserService.getUserInformationWithoutImageDecompression(theDTO.getUsername());

        if (theDTO.getUserPassword() != null) {
            //if the client has sent a password to be updated, hash it and save it.
            userInfo.setUserPassword(theUserService.encodePassword(theDTO.getUserPassword()));
        }
        userInfo.setContactNumber(theDTO.getContactNumber().trim()); //set the new contact number
        theUserService.updateUserInformation(userInfo); //call the update method

        return new ResponseEntity<>(new BangerAndCoResponse("User Updated Successfully", HttpStatus.OK.value()), HttpStatus.OK);
    }
}
