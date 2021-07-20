package com.lakindu.bangerandcobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakindu.bangerandcobackend.dto.UserAdminCreateDTO;
import com.lakindu.bangerandcobackend.dto.UserUpdateDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.RentalService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.*;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/api/user")
public class UserController {
    private final UserService theUserService;
    private final RentalService rentalService;
    private final Validator validator;

    @Autowired //dependency injection handled by entity manager
    public UserController(
            @Qualifier("userServiceImpl") UserService theUserService,
            @Qualifier("rentalServiceImpl") RentalService rentalService,
            @Qualifier("defaultValidator") Validator validator
    ) {
        this.theUserService = theUserService;
        this.rentalService = rentalService;
        this.validator = validator;
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
    public ResponseEntity<BangerAndCoResponse> updateUser(@Valid @RequestBody UserUpdateDTO theDTO) throws ResourceNotFoundException, BadValuePassedException, ResourceAlreadyExistsException {
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
    @PutMapping(path = "/customer/whitelist")
    public ResponseEntity<BangerAndCoResponse> whiteListCustomer(@RequestBody(required = true) HashMap<String, String> theCustomer) throws ResourceNotFoundException, ResourceNotUpdatedException {
        //method executed by the administrator to white list the customer so that they can make rentals again.
        //user gets blacklisted by the system automatically when they fail to collect their made rental.
        User whiteListedCustomer = theUserService.whitelistCustomer(theCustomer.get("username"));
        return new ResponseEntity<>(
                new BangerAndCoResponse(String.format("%s %s Has Been Whitelisted Successfully", whiteListedCustomer.getFirstName(), whiteListedCustomer.getLastName()), HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping(path = "/getLicense/{username}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getLicenseImage(@PathVariable(name = "username") String username) throws DataFormatException, IOException {
        return new ResponseEntity<>(theUserService.getCustomerLicenseImage(username), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping(path = "/getOther/{username}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getOtherIdentity(@PathVariable(name = "username") String username) throws DataFormatException, IOException {
        return new ResponseEntity<>(theUserService.getCustomerOtherImage(username), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PutMapping(path = "/update/license/{customerUserName}")
    public ResponseEntity<BangerAndCoResponse> updateLicenseImage(@RequestParam(name = "licenseImage") MultipartFile licenseImage,
                                                                  @PathVariable(name = "customerUserName") String customerUsername,
                                                                  Authentication loggedInUser) throws DataFormatException, IOException, ResourceNotUpdatedException {
        //method executed by the customer when they update their license image.
        //use the authentication object in the security context to check usernames
        theUserService.updateCustomerLicenseImage(customerUsername, licenseImage, loggedInUser);
        return new ResponseEntity<>(
                new BangerAndCoResponse("The license image has been updated successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PutMapping(path = "/update/otherIdentity/{customerUserName}")
    public ResponseEntity<BangerAndCoResponse> updateOtherIdentity(@RequestParam(name = "otherImage") MultipartFile licenseImage,
                                                                   @PathVariable(name = "customerUserName") String customerUsername,
                                                                   Authentication loggedInUser) throws DataFormatException, IOException, ResourceNotUpdatedException {
        //method executed by the customer when they update their license image.
        //use the authentication object in the security context to check usernames
        theUserService.updateCustomerOtherImage(customerUsername, licenseImage, loggedInUser);
        return new ResponseEntity<>(
                new BangerAndCoResponse("The other identity image has been updated successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping(path = "/blacklist")
    public ResponseEntity<BangerAndCoResponse> blackListCustomers() throws ParseException {
        rentalService.blacklistCustomers();
        return new ResponseEntity<>(
                new BangerAndCoResponse("The blacklist operation has been operated successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping(path = "/admin/getAllAdmins")
    public ResponseEntity<List<UserDTO>> getAllAdmins() throws DataFormatException, IOException {
        //method will retrieve a list of all available administrators from the system.
        List<UserDTO> allAdministrators = theUserService.getAllAdministrators();
        return new ResponseEntity<>(allAdministrators, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping(path = "/admin/delete/{username}")
    public ResponseEntity<BangerAndCoResponse> deleteAdministrator(
            @PathVariable(name = "username") String username,
            Authentication loggedInUser
    ) throws ResourceCannotBeDeletedException, ResourceNotFoundException {
        //method executed by administrator to remove their account.

        theUserService.removeAdministrator(username, loggedInUser);
        return new ResponseEntity<>(
                new BangerAndCoResponse("The administrator account has been removed from Banger and Co. successfully", HttpStatus.OK.value()),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping(
            path = "/admin/createAdmin",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BangerAndCoResponse> createAdministrator(
            @RequestParam(name = "profilePic", required = true) MultipartFile profilePicture,
            @RequestParam(name = "userInfo", required = true) String profileInformation
    ) throws IOException, DataFormatException, ResourceAlreadyExistsException, ResourceNotFoundException, ResourceNotCreatedException {

        ObjectMapper theMapper = new ObjectMapper();
        UserAdminCreateDTO createdDTO = theMapper.readValue(profileInformation, UserAdminCreateDTO.class);

        DataBinder theBinder = new DataBinder(createdDTO);
        theBinder.addValidators((org.springframework.validation.Validator) validator);
        theBinder.validate();

        BindingResult bindingResult = theBinder.getBindingResult();

        if (bindingResult.hasErrors()) {
            //if the entity class does not meet the expected validations
            throw new ValidationException("Valid inputs were not provided for the fields during Sign Up.");
        } else {
            createdDTO.setEmailAddress(createdDTO.getEmailAddress().trim());
            createdDTO.setUsername(createdDTO.getUsername().trim());
            createdDTO.setContactNumber(createdDTO.getContactNumber().trim());
            createdDTO.setEmailAddress(createdDTO.getEmailAddress().trim());
            createdDTO.setLastName(createdDTO.getLastName().trim());
            createdDTO.setFirstName(createdDTO.getFirstName().trim());
            createdDTO.setProfilePicture(profilePicture.getBytes());
            createdDTO.setUserPassword(createdDTO.getUsername()); //assign the admin password as their username

            theUserService.createAdmin(createdDTO);

            return new ResponseEntity<>(
                    new BangerAndCoResponse("The administrator account has been successfully created and the user has been notified via an email.", HttpStatus.OK.value()),
                    HttpStatus.OK
            );
        }
    }
}
