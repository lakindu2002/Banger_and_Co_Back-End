package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.repository.RoleRepository;
import com.lakindu.bangerandcobackend.repository.UserRepository;
import com.lakindu.bangerandcobackend.util.FileHandler.CompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import com.lakindu.bangerandcobackend.util.UserAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.mailsender.MailSender;
import com.lakindu.bangerandcobackend.util.mailsender.MailSenderHelper;
import com.lakindu.bangerandcobackend.util.mailsender.MailTemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ValidationException;
import javax.validation.Validator;

@Service
public class UserService {
    private final UserRepository theUserRepository;
    private final RoleRepository theRoleRepository;
    private final Validator validator;
    private final MailSender theSender;

    @Autowired
    public UserService(UserRepository theUserRepository, RoleRepository theRoleRepository, Validator validator, MailSender theSender) {
        this.theUserRepository = theUserRepository;
        this.theRoleRepository = theRoleRepository;
        this.validator = validator;
        this.theSender = theSender;
    }

    public User createUser(User theNewUser, MultipartFile profilePicture) throws Exception {
        //method used to create a User

        //check if user exists
        final User existingUser = theUserRepository.findUserByEmailAddress(theNewUser.getEmailAddress().toLowerCase());

        if (existingUser == null) {
            //if user doesn't exist
            theNewUser.setEmailAddress(theNewUser.getEmailAddress().toLowerCase()); //save the lower case email in DB

            //validate the Entity class for errors.
            DataBinder theDataBinder = new DataBinder(theNewUser);
            theDataBinder.addValidators((org.springframework.validation.Validator) validator);
            theDataBinder.validate();

            final BindingResult theBindingResult = theDataBinder.getBindingResult();

            if (theBindingResult.hasErrors()) {
                //if the entity class does not meet the expected validations
                throw new ValidationException("Valid inputs were not provided for the fields during Sign Up.");
            } else {
                //if successfully validated
                theNewUser.setProfilePicture(profilePicture.getBytes());
                ImageHandler theCompressor = new CompressImage(); //creating template method pattern
                theCompressor.processUnhandledImage(theNewUser); //calling the template method

                //retrieve role information for the customer
                Role theRole = theRoleRepository.findRoleByRoleName("customer");

                if (theRole != null) {
                    //if the role has been retrieved successfully
                    theNewUser.setUserRole(theRole);
                    theNewUser.setBlackListed(false);

                    final User registeredUser = theUserRepository.save(theNewUser);
                    theSender.sendMail(new MailSenderHelper(registeredUser, "Welcome To Banger and Co!", MailTemplateType.SIGNUP));
                    return registeredUser;
                } else {
                    throw new UnsupportedOperationException("invalid role assignment");
                }
            }
        } else {
            throw new UserAlreadyExistsException(String.format("an account already exists with - %s", theNewUser.getEmailAddress()));
        }
    }
}
