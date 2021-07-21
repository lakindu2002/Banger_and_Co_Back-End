package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.auth.CustomUserPrincipal;
import com.lakindu.bangerandcobackend.dto.UserAdminCreateDTO;
import com.lakindu.bangerandcobackend.dto.UserUpdateDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.repository.UserRepository;
import com.lakindu.bangerandcobackend.serviceinterface.RoleService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.*;
import com.lakindu.bangerandcobackend.util.mailsender.MailSender;
import com.lakindu.bangerandcobackend.util.mailsender.MailSenderHelper;
import com.lakindu.bangerandcobackend.util.mailsender.MailTemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository theUserRepository;
    private final RoleService theRoleService;
    private final MailSender theSender;
    private final PasswordEncoder passwordEncoder;
    private Logger LOGGER;

    @Autowired
    public UserServiceImpl(
            @Qualifier("userRepository") UserRepository theUserRepository,
            @Qualifier("roleServiceImpl") RoleService theRoleService,
            @Qualifier("mailSender") MailSender theSender,
            @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder) {
        this.theUserRepository = theUserRepository;
        this.theRoleService = theRoleService;
        this.theSender = theSender;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //method defined by Spring Security and is called by SpringSecurity when user needs to be authenticated
        //email address = username

        User theUser = theUserRepository.findUserByUsername(username);
        if (theUser != null) {
            return new CustomUserPrincipal(theUser); //this class implements UserDetails therefore it can be returned
        } else {
            //spring returns a BadCredentialException that is of 500 error code, so in angular dealing as 500 error code.
            //user does not exist
            throw new UsernameNotFoundException("Invalid Username or Password");
        }

    }

    @Override
    public User _getUserWithImageDecompression(String username) throws DataFormatException, IOException, ResourceNotFoundException {
        //get logged in user information by accessing database
        User theUser = theUserRepository.findUserByUsername(username);
        if (theUser != null) {
            final byte[] decompressedImage = new ImageHandler().decompressImage(theUser.getProfilePicture());
            theUser.setProfilePicture(decompressedImage);

            return theUser;
        } else {
            throw new ResourceNotFoundException("The Username Provided Does Not Exist.");
        }
    }

    @Override
    public User _getUserWithoutDecompression(String username) {
        return theUserRepository.findUserByUsername(username);
    }

    @Override
    public String _getUserRole(String userName) {
        return theUserRepository.getUserRole(userName);
    }

    @Override
    public UserDTO getUserInformation(String username) throws Exception {
        final User theUser = theUserRepository.findUserByUsername(username);
        if (theUser == null) {
            throw new ResourceNotFoundException("The username does not exist");
        } else {
            final byte[] decompressedImage = new ImageHandler().decompressImage(theUser.getProfilePicture());

            UserDTO theDTO = new UserDTO();
            theDTO.setFirstName(theUser.getFirstName());
            theDTO.setLastName(theUser.getLastName());
            theDTO.setUsername(theUser.getUsername());
            theDTO.setEmailAddress(theUser.getEmailAddress());
            //images are binary data (byte[])
            theDTO.setProfilePicture(decompressedImage); //jackson will automatically convert byte[] to base64 via data binding
            theDTO.setUserRole(theUser.getUserRole().getRoleName());
            theDTO.setDateOfBirth(theUser.getDateOfBirth());
            theDTO.setBlackListed(theUser.isBlackListed());
            theDTO.setContactNumber(theUser.getContactNumber());

            if (theUser.getUserRole().getRoleName().equalsIgnoreCase("customer")) {
                theDTO.setDrivingLicenseNumber(theUser.getDrivingLicenseNumber());
            }
            return theDTO;
        }
    }

    @Override
    @Transactional
    public void createUser(UserDTO theNewUser, MultipartFile profilePicture, MultipartFile licensePic, MultipartFile otherIdentity) throws IOException, ResourceNotFoundException, DataFormatException, ResourceAlreadyExistsException, ResourceNotCreatedException {
        theNewUser.setUsername(theNewUser.getUsername());
        theNewUser.setEmailAddress(theNewUser.getEmailAddress().toLowerCase());
        theNewUser.setBlackListed(false);
        theNewUser.setUserPassword(encodePassword(theNewUser.getUserPassword()));
        theNewUser.setProfilePicture(profilePicture.getBytes());
        theNewUser.setLicensePic(licensePic.getBytes());
        theNewUser.setOtherIdentity(otherIdentity.getBytes());

        Role theRole = theRoleService._getRoleInformation("customer");

        //does username exist
        User userNameExists = theUserRepository.findUserByUsername(theNewUser.getUsername());

        User entityToBeSaved = User.convertDTOToEntity(theNewUser, theRole); //convert DTO to entity.
        entityToBeSaved.setDrivingLicenseNumber(theNewUser.getDrivingLicenseNumber());

        if (userNameExists == null) {
            //does email exist
            User emailExists = theUserRepository.findUserByEmailAddress(theNewUser.getEmailAddress());
            if (emailExists == null) {
                //email valid

                if (theUserRepository.findUserByDrivingLicenseNumberEquals(entityToBeSaved.getDrivingLicenseNumber()) == null) {
                    //license number does not exist in DB
                    //compress the images before saving.
                    entityToBeSaved.setProfilePicture(new ImageHandler().compressImage(theNewUser.getProfilePicture()));
                    entityToBeSaved.setDrivingLicense(new ImageHandler().compressImage(theNewUser.getLicensePic()));
                    entityToBeSaved.setOtherIdentity(new ImageHandler().compressImage(theNewUser.getOtherIdentity()));

                    User registeredUser = theUserRepository.save(entityToBeSaved);
                    //save user and send welcome email
                    try {
                        theSender.sendMail(new MailSenderHelper(registeredUser, "Welcome To Banger and Co!", MailTemplateType.SIGNUP));
                    } catch (Exception ex) {
                        System.out.println("ERROR SENDING EMAIL");
                    }
                } else {
                    throw new ResourceAlreadyExistsException("An account already exists that has the same driving license number that you provided");
                }
            } else {
                throw new ResourceAlreadyExistsException("This email address is already associated to an account.");
            }
        } else {
            throw new ResourceAlreadyExistsException("An account already exists with the username that you provided");
        }
    }

    @Override
    @Transactional
    public void updateUserInformation(UserUpdateDTO userInfo) throws ResourceNotFoundException, BadValuePassedException, ResourceAlreadyExistsException {
        //updated the user information and send an email.
        if (theUserRepository.existsById(userInfo.getUsername())) {
            final User updatingUser = _getUserWithoutDecompression(userInfo.getUsername()); //retrieve the user information
            if (userInfo.getUserPassword() != null) {
                //if the client has sent a password to be updated, hash it and save it.
                //check if password sent is between 6 and 15 characters
                if (userInfo.getUserPassword().length() >= 6 && userInfo.getUserPassword().length() <= 15) {
                    updatingUser.setUserPassword(encodePassword(userInfo.getUserPassword()));
                } else {
                    //do not allow password updating
                    throw new BadValuePassedException("The password is not between 6 and 15 characters. Please make sure password is between 6 and 15 characters");
                }
            }
            if (userInfo.getDrivingLicenseNumber() != null) {
                //if the client has sent a license number to be updated, save if after checking if any other account has the number
                if (theUserRepository.getUserByLicenseNumberForOther(userInfo.getUsername(), userInfo.getDrivingLicenseNumber()) == null) {
                    updatingUser.setDrivingLicenseNumber(userInfo.getDrivingLicenseNumber());
                } else {
                    throw new ResourceAlreadyExistsException("The new driving license number already belongs to another customer at Banger and Co.");
                }
            }
            updatingUser.setContactNumber(userInfo.getContactNumber().trim()); //set the new contact number
            final User updatedUser = theUserRepository.save(updatingUser);
            theSender.sendMail(new MailSenderHelper(
                    updatedUser,
                    "Account Details Updated Successfully!",
                    MailTemplateType.UPDATEACCOUNT
            ));
        } else {
            throw new ResourceNotFoundException("The username that you are trying to update does not exist in the system");
        }
    }

    @Override
    public List<UserDTO> getAllCustomers() throws DataFormatException, IOException {
        //method will return all the customers that can be viewed by the administrator.
        List<User> theCustomersInDb = theUserRepository.getAllUsersExceptAdministrator("administrator"); //return all users in database except administrator.
        //construct a DTO for each user and add to the return list that can be viewed by the admin.
        return convertToDTOList(theCustomersInDb);

    }

    private List<UserDTO> convertToDTOList(List<User> dbList) throws DataFormatException, IOException {
        List<UserDTO> theUserList = new ArrayList<>();
        for (User eachCustomer : dbList) {
            UserDTO theUser = new UserDTO();

            theUser.setUsername(eachCustomer.getUsername());
            theUser.setEmailAddress(eachCustomer.getEmailAddress());
            theUser.setFirstName(eachCustomer.getFirstName());
            theUser.setLastName(eachCustomer.getLastName());
            theUser.setDateOfBirth(eachCustomer.getDateOfBirth());
            theUser.setUserPassword(null);
            theUser.setContactNumber(eachCustomer.getContactNumber());
            theUser.setUserRole(eachCustomer.getUserRole().getRoleName());
            theUser.setProfilePicture(new ImageHandler().decompressImage((eachCustomer.getProfilePicture())));
            theUser.setBlackListed(eachCustomer.isBlackListed());

            theUserList.add(theUser); //attach the entitys DTO to the DTO List.
        }
        return theUserList;
    }

    @Override
    @Transactional
    public User whitelistCustomer(String username) throws ResourceNotFoundException, ResourceNotUpdatedException {
        //first load the user information.
        User theCustomer = theUserRepository.findUserByUsername(username); //load customer.
        if (theCustomer == null) {
            //customer does not exist
            throw new ResourceNotFoundException("A customer with the username provided does not exist at Banger and Co.");
        } else {
            //first check if the user is indeed a customer.
            if (theCustomer.getUserRole().getRoleName().equalsIgnoreCase("customer")) {
                //user is indeed a customer.
                //check if the user is BLACKLISTED. if user is blacklisted
                if (theCustomer.isBlackListed()) {
                    //user is blacklisted, go ahead and whitelist the customer.
                    theCustomer.setBlackListed(false); //whitelist the customer to allow them to make rentals.
                    User whiteListedCustomer = theUserRepository.save(theCustomer);//update the user information in the database.

                    //send an email to the whitelisted customer to denote they have access to make a rental.
                    theSender.sendMail(new MailSenderHelper(
                            whiteListedCustomer, "Account Whitelisted Successfully", MailTemplateType.WHITELIST)
                    );

                    return whiteListedCustomer;
                } else {
                    //user is not blacklisted, no point proceeding.
                    throw new ResourceNotUpdatedException("The customer is already whitelisted.");
                }
            } else {
                throw new ResourceNotFoundException("A customer with the username provided does not exist at Banger and Co");
            }
        }
    }

    @Override
    public void updateCustomerLicenseImage(String customerUsername, MultipartFile licenseImage, Authentication loggedInUser) throws ResourceNotUpdatedException, IOException, DataFormatException {
        //check if passed username is username attached in token.
        if (loggedInUser.getName().equals(customerUsername)) {
            //usernames are same

            //retrieve the user information
            User updatingUser = _getUserWithoutDecompression(customerUsername);
            updatingUser.setDrivingLicense(new ImageHandler().compressImage(licenseImage.getBytes()));
            theUserRepository.save(updatingUser); //update the license image.

            //send email
            try {
                theSender.sendMail(new MailSenderHelper(
                        updatingUser,
                        "License Image Updated Successfully",
                        MailTemplateType.UPDATEACCOUNT
                ));
            } catch (Exception ex) {
                LOGGER.warning("EMAIL NOT SENT WHILE LICENSE UPDATE: " + ex.getMessage());
            }
        } else {
            throw new ResourceNotUpdatedException("The account that you are trying to update does not belong to you");
        }
    }

    @Override
    public byte[] getCustomerLicenseImage(String username) throws DataFormatException, IOException {
        return new ImageHandler().decompressImage(theUserRepository.getUserByUsername(username).getDrivingLicense());
    }

    @Override
    public byte[] getCustomerOtherImage(String username) throws DataFormatException, IOException {
        return new ImageHandler().decompressImage(theUserRepository.getUserByUsername(username).getOtherIdentity());
    }

    @Override
    public void updateCustomerOtherImage(String customerUsername, MultipartFile otherIdentity, Authentication loggedInUser) throws ResourceNotUpdatedException, IOException, DataFormatException {
        //check if passed username is username attached in token.
        if (loggedInUser.getName().equals(customerUsername)) {
            //usernames are same

            //retrieve the user information
            User updatingUser = _getUserWithoutDecompression(customerUsername);
            updatingUser.setOtherIdentity(new ImageHandler().compressImage((otherIdentity.getBytes())));
            theUserRepository.save(updatingUser); //update the license image.

            //send email
            try {
                theSender.sendMail(new MailSenderHelper(
                        updatingUser,
                        "Other Identity Image Updated Successfully",
                        MailTemplateType.UPDATEACCOUNT
                ));
            } catch (Exception ex) {
                LOGGER.warning("EMAIL NOT SENT WHILE OTHER IMAGE UPDATE: " + ex.getMessage());
            }
        } else {
            throw new ResourceNotUpdatedException("The account that you are trying to update does not belong to you");
        }
    }

    /**
     * Method will return a list of all available administrators in the system.
     *
     * @return List of admins at Banger and Co.
     */
    @Override
    public List<UserDTO> getAllAdministrators() throws DataFormatException, IOException {
        List<User> administratorList = theUserRepository.getAllAdministrators("administrator");
        return convertToDTOList(administratorList);
    }

    /**
     * Method will create an administrator account at Banger And Co.
     *
     * @param createdDTO The administrator to be created.
     */
    @Override
    public void createAdmin(UserAdminCreateDTO createdDTO) throws ResourceAlreadyExistsException, DataFormatException, IOException, ResourceNotFoundException, ResourceNotCreatedException {
        createdDTO.setUsername(createdDTO.getUsername());
        createdDTO.setEmailAddress(createdDTO.getEmailAddress().toLowerCase());
        createdDTO.setUserPassword(encodePassword(createdDTO.getUserPassword()));

        //check if email or username exists.
        User userByUsername = theUserRepository.findUserByUsername(createdDTO.getUsername());
        User userByEmailAddress = theUserRepository.findUserByEmailAddress(createdDTO.getEmailAddress());

        if (userByUsername != null) {
            throw new ResourceAlreadyExistsException("This username is already taken");
        }
        if (userByEmailAddress != null) {
            throw new ResourceAlreadyExistsException("There is already an account with this email address associated to it");
        }

        User theAdmin = new User();
        Role administratorRole = theRoleService._getRoleInformation("administrator");

        theAdmin.setUsername(createdDTO.getUsername());
        theAdmin.setEmailAddress(createdDTO.getEmailAddress());
        theAdmin.setFirstName(createdDTO.getFirstName());
        theAdmin.setLastName(createdDTO.getLastName());
        theAdmin.setDateOfBirth(createdDTO.getDateOfBirth());
        theAdmin.setUserPassword(createdDTO.getUserPassword());
        theAdmin.setContactNumber(createdDTO.getContactNumber());
        theAdmin.setProfilePicture(new ImageHandler().compressImage(createdDTO.getProfilePicture()));
        theAdmin.setBlackListed(false);
        theAdmin.setUserRole(administratorRole);

        User savedUser = theUserRepository.save(theAdmin);

        //send an email to created admin.
        try {
            theSender.sendMail(new MailSenderHelper(
                    savedUser, "Account Created Successfully", MailTemplateType.ADMIN_CREATED
            ));
        } catch (Exception ex) {
            LOGGER.warning("FAILED TO SEND EMAIL");
        }
    }

    @Override
    public void removeAdministrator(String username, Authentication loggedInUser) throws ResourceCannotBeDeletedException, ResourceNotFoundException {
        if (!username.equalsIgnoreCase(loggedInUser.getName())) {
            //admins can only remove their own account
            User deletingAdministrator = theUserRepository.findUserByUsername(username);
            if (deletingAdministrator.getUserRole().getRoleName().equalsIgnoreCase("administrator")) {
                //clear any resolved inquiries before removing the admin account
                if (theRoleService._getRoleInformation("administrator").getUsersInEachRole().size() == 1) {
                    //only one admin left
                    throw new ResourceCannotBeDeletedException("There is only one administrator account present in the system. Therefore, your account cannot be removed");
                }
                deletingAdministrator.clearInquiriesResolved();
                theUserRepository.delete(deletingAdministrator);

                //send an email.
                try {
                    theSender.sendMail(
                            new MailSenderHelper(deletingAdministrator, "Administrator Account Deleted", MailTemplateType.ADMIN_DELETED)
                    );
                } catch (Exception ex) {
                    LOGGER.warning("ERROR SENDING EMAIL: " + ex.getMessage());
                }

            } else {
                throw new ResourceCannotBeDeletedException("This account is not an administrator account");
            }

        } else {
            throw new ResourceCannotBeDeletedException("You cannot delete your own administrator account");
        }
    }

    /**
     * Method will change the user status to blacklisted = true and will email the user to inform that they have been blacklisted.
     *
     * @param theCustomerNotCollected The customer going to be blacklisted
     * @param theRentalNotCollected   The rental that they did not collect
     */
    @Override
    @Transactional
    public void blackListCustomer(String theCustomerNotCollected, Rental theRentalNotCollected) {
        User userByUsername = theUserRepository.getUserByUsername(theCustomerNotCollected);
        //if user is already blacklisted do not send email again
        if (!userByUsername.isBlackListed()) {
            userByUsername.setBlackListed(true);
            theUserRepository.save(userByUsername);
            try {
                theSender.sendRentalMail(
                        new MailSenderHelper(
                                userByUsername, "Account Blacklisted", MailTemplateType.ACCOUNT_BLACKLISTED
                        ), theRentalNotCollected
                );
            } catch (IOException | MessagingException e) {
                LOGGER.warning("EMAIL NOT SENT TO BLACKLISTED CUSTOMER");
            }
        }
    }

    @Override
    public List<String> _getAllAdminEmails() throws ResourceNotFoundException {
        List<User> administrator = theUserRepository.getUsersByUserRoleEquals(theRoleService._getRoleInformation("administrator"));
        List<String> emailList;

        emailList = administrator.stream().map((eachUser) -> {
            return eachUser.getEmailAddress();
        }).collect(Collectors.toList());

        return emailList;
    }

    @Override
    public String encodePassword(String password) {
        //encode the password using the password encoder BCrypt.
        return passwordEncoder.encode(password);
    }
}
