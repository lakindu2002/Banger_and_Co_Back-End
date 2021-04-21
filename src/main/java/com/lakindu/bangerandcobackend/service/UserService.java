package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.auth.CustomUserPrincipal;
import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.repository.UserRepository;
import com.lakindu.bangerandcobackend.util.FileHandler.CompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.DecompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import com.lakindu.bangerandcobackend.util.exceptionhandling.UserAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.mailsender.MailSender;
import com.lakindu.bangerandcobackend.util.mailsender.MailSenderHelper;
import com.lakindu.bangerandcobackend.util.mailsender.MailTemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;


@Service
public class UserService implements UserDetailsService {
    private final UserRepository theUserRepository;
    private final RoleService theRoleService;
    private final MailSender theSender;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository theUserRepository, RoleService theRoleService, MailSender theSender, PasswordEncoder passwordEncoder) {
        this.theUserRepository = theUserRepository;
        this.theRoleService = theRoleService;
        this.theSender = theSender;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserInformation(String username) throws Exception {
        //get logged in user information by accessing database
        User theUser = theUserRepository.findUserByUsername(username);
        if (theUser != null) {
            ImageHandler theDecompressor = new DecompressImage();
            theDecompressor.processUnhandledImage(theUser);
            return theUser;
        } else {
            return null;
        }
    }

    public User findLoggingInUser(String username) throws Exception {
        final User retrievedUser = theUserRepository.findUserByUsername(username);
        ImageHandler theDecompressor = new DecompressImage();
        theDecompressor.processUnhandledImage(retrievedUser);
        return retrievedUser;
    }

    @Transactional
    public User createUser(User theNewUser, MultipartFile profilePicture) throws Exception {
        //method used to create a User
        theNewUser.setUsername(theNewUser.getUsername().toLowerCase());
        theNewUser.setEmailAddress(theNewUser.getEmailAddress().toLowerCase());
        //check if user exists
        User userNameExists = theUserRepository.findUserByUsername(theNewUser.getUsername().toLowerCase());

        if (userNameExists == null) {
            //if username is valid

            //check if email address is associated to any account
            User emailExists = theUserRepository.findUserByEmailAddress(theNewUser.getEmailAddress().toLowerCase());
            if (emailExists != null) {
                throw new UserAlreadyExistsException("This email address is already associated to an account.");
            } else {
                theNewUser.setProfilePicture(profilePicture.getBytes());
                ImageHandler theCompressor = new CompressImage(); //creating template method pattern
                theCompressor.processUnhandledImage(theNewUser); //calling the template method

                //retrieve role information for the customer
                Role theRole = theRoleService.getRoleInformation("customer");

                if (theRole != null) {
                    //if the role has been retrieved successfully
                    theNewUser.setUserRole(theRole);
                    theNewUser.setBlackListed(false);
                    //encode the password to store with encryption
                    theNewUser.setUserPassword(passwordEncoder.encode(theNewUser.getUserPassword()));
                    final User registeredUser = theUserRepository.save(theNewUser);
                    System.out.println("--------------USER SAVED-----------------");
                    theSender.sendMail(new MailSenderHelper(registeredUser, "Welcome To Banger and Co!", MailTemplateType.SIGNUP));
                    System.out.println("--------------EMAIL SENT--------------");
                    return registeredUser;
                } else {
                    throw new UnsupportedOperationException("invalid role assignment");
                }

            }
        } else {
            throw new UserAlreadyExistsException("An account already exists with the username that you provided");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //method defined by Spring Security and is called by SpringSecurity when user needs to be authenticated
        //email address = username

        User theUser = theUserRepository.findUserByUsername(username.toLowerCase());
        if (theUser != null) {
            return new CustomUserPrincipal(theUser); //this class implements UserDetails therefore it can be returned
        } else {
            //user does not exist
            throw new UsernameNotFoundException("Invalid Email Address or Password");
        }

    }
}
