package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.auth.CustomUserPrincipal;
import com.lakindu.bangerandcobackend.dto.UserUpdateDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.repository.UserRepository;
import com.lakindu.bangerandcobackend.serviceinterface.RoleService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.FileHandler.CompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.DecompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;
import com.lakindu.bangerandcobackend.util.mailsender.MailSender;
import com.lakindu.bangerandcobackend.util.mailsender.MailSenderHelper;
import com.lakindu.bangerandcobackend.util.mailsender.MailTemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository theUserRepository;
    private final RoleService theRoleService;
    private final MailSender theSender;
    private final PasswordEncoder passwordEncoder;

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //method defined by Spring Security and is called by SpringSecurity when user needs to be authenticated
        //email address = username

        User theUser = theUserRepository.findUserByUsername(username);
        if (theUser != null) {
            return new CustomUserPrincipal(theUser); //this class implements UserDetails therefore it can be returned
        } else {
            //user does not exist
            throw new UsernameNotFoundException("Invalid Email Address or Password");
        }

    }

    @Override
    public User _getUserWithImageDecompression(String username) throws DataFormatException, IOException, ResourceNotFoundException {
        //get logged in user information by accessing database
        User theUser = theUserRepository.findUserByUsername(username);
        if (theUser != null) {
            ImageHandler theDecompressor = new DecompressImage();
            final byte[] decompressedImage = theDecompressor.processUnhandledImage(theUser.getProfilePicture());
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
    public UserDTO getUserInformation(String username) throws Exception {
        final User theUser = theUserRepository.findUserByUsername(username);
        if (theUser == null) {
            throw new ResourceNotFoundException("The username does not exist");
        } else {
            ImageHandler theDecompressor = new DecompressImage();
            final byte[] decompressedImage = theDecompressor.processUnhandledImage(theUser.getProfilePicture());
            theUser.setProfilePicture(decompressedImage);

            UserDTO theDTO = new UserDTO();
            theDTO.setFirstName(theUser.getFirstName());
            theDTO.setLastName(theUser.getLastName());
            theDTO.setUsername(theUser.getUsername());
            theDTO.setEmailAddress(theUser.getEmailAddress());
            //images are binary data (byte[])
            theDTO.setProfilePicture(theUser.getProfilePicture()); //jackson will automatically convert byte[] to base64 via data binding
            theDTO.setUserRole(theUser.getUserRole().getRoleName());
            theDTO.setDateOfBirth(theUser.getDateOfBirth());
            theDTO.setBlackListed(theUser.isBlackListed());
            theDTO.setContactNumber(theUser.getContactNumber());
            return theDTO;
        }
    }

    @Override
    @Transactional
    public void createUser(UserDTO theNewUser, MultipartFile profilePicture) throws IOException, ResourceNotFoundException, DataFormatException, ResourceAlreadyExistsException {
        theNewUser.setUsername(theNewUser.getUsername());
        theNewUser.setEmailAddress(theNewUser.getEmailAddress().toLowerCase());
        theNewUser.setBlackListed(false);
        theNewUser.setUserPassword(encodePassword(theNewUser.getUserPassword()));
        theNewUser.setProfilePicture(profilePicture.getBytes());

        Role theRole = theRoleService._getRoleInformation("customer");

        //does username exist
        User userNameExists = theUserRepository.findUserByUsername(theNewUser.getUsername());

        User entityToBeSaved = User.convertDTOToEntity(theNewUser, theRole); //convert DTO to entity.

        ImageHandler theCompressor = new CompressImage(); //creating template method pattern
        final byte[] compressImage = theCompressor.processUnhandledImage(entityToBeSaved.getProfilePicture());//calling the template method
        entityToBeSaved.setProfilePicture(compressImage);

        if (userNameExists == null) {
            //does email exist
            User emailExists = theUserRepository.findUserByEmailAddress(theNewUser.getEmailAddress());
            if (emailExists == null) {
                //email valid
                User registeredUser = theUserRepository.save(entityToBeSaved);
                //save user and send welcome email
                theSender.sendMail(new MailSenderHelper(registeredUser, "Welcome To Banger and Co!", MailTemplateType.SIGNUP));
            } else {
                throw new ResourceAlreadyExistsException("This email address is already associated to an account.");
            }
        } else {
            throw new ResourceAlreadyExistsException("An account already exists with the username that you provided");
        }
    }

    @Override
    @Transactional
    public void updateUserInformation(UserUpdateDTO userInfo) throws ResourceNotFoundException, BadValuePassedException {
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
        List<UserDTO> theCustomerList = new ArrayList<>();

        for (User eachCustomer : theCustomersInDb) {
            //construct a DTO for each user and add to the return list that can be viewed by the admin.
            UserDTO theCustomerDTO = new UserDTO();

            theCustomerDTO.setUsername(eachCustomer.getUsername());
            theCustomerDTO.setEmailAddress(eachCustomer.getEmailAddress());
            theCustomerDTO.setFirstName(eachCustomer.getFirstName());
            theCustomerDTO.setLastName(eachCustomer.getLastName());
            theCustomerDTO.setDateOfBirth(eachCustomer.getDateOfBirth());
            theCustomerDTO.setUserPassword(null);
            theCustomerDTO.setContactNumber(eachCustomer.getContactNumber());
            theCustomerDTO.setUserRole(eachCustomer.getUserRole().getRoleName());
            theCustomerDTO.setProfilePicture(new DecompressImage().processUnhandledImage(eachCustomer.getProfilePicture()));
            theCustomerDTO.setBlackListed(eachCustomer.isBlackListed());

            theCustomerList.add(theCustomerDTO); //attach the entitys DTO to the DTO List.
        }
        return theCustomerList;
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
            if (theCustomer.getUserRole().getRoleName().toLowerCase().equalsIgnoreCase("customer")) {
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
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
