package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.auth.CustomUserPrincipal;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.repository.UserRepository;
import com.lakindu.bangerandcobackend.serviceinterface.RoleService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.FileHandler.CompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.DecompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.UserAlreadyExistsException;
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


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository theUserRepository;
    private final RoleService theRoleService;
    private final MailSender theSender;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
            UserRepository theUserRepository,
            @Qualifier("roleServiceImpl") RoleService theRoleService,
            MailSender theSender,
            PasswordEncoder passwordEncoder) {
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

    @Override
    public User getUserInformationWithoutImageDecompression(String username) throws Exception {
        return theUserRepository.findUserByUsername(username);
    }

    @Override
    @Transactional
    public User createUser(UserDTO theNewUser, MultipartFile profilePicture) throws Exception {
        theNewUser.setUsername(theNewUser.getUsername());
        theNewUser.setEmailAddress(theNewUser.getEmailAddress().toLowerCase());
        theNewUser.setBlackListed(false);
        theNewUser.setUserPassword(encodePassword(theNewUser.getUserPassword()));
        theNewUser.setProfilePicture(profilePicture.getBytes());

        Role theRole = theRoleService.getRoleInformation("customer");

        //does username exist
        User userNameExists = theUserRepository.findUserByUsername(theNewUser.getUsername());

        if (userNameExists == null) {
            //does email exist
            User emailExists = theUserRepository.findUserByEmailAddress(theNewUser.getEmailAddress());
            if (emailExists == null) {
                //email valid
                if (theRole == null) {
                    //if role is not in DB, throw an exception to display error to user
                    throw new UnsupportedOperationException("The Role Does Not Exist In The System");
                } else {
                    User entityToBeSaved = User.convertDTOToEntity(theNewUser, theRole);
                    ImageHandler theCompressor = new CompressImage(); //creating template method pattern
                    theCompressor.processUnhandledImage(entityToBeSaved); //calling the template method

                    User registeredUser = theUserRepository.save(entityToBeSaved);
                    //save user and send welcome email
                    theSender.sendMail(new MailSenderHelper(registeredUser, "Welcome To Banger and Co!", MailTemplateType.SIGNUP));
                    return registeredUser;
                }
            } else {
                throw new UserAlreadyExistsException("This email address is already associated to an account.");
            }
        } else {
            throw new UserAlreadyExistsException("An account already exists with the username that you provided");
        }
    }

    @Override
    @Transactional
    public void updateUserInformation(User userInfo) {
        //updated the user information and send an email.
        if (theUserRepository.existsById(userInfo.getUsername())) {
            final User updatedUser = theUserRepository.save(userInfo);
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
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
