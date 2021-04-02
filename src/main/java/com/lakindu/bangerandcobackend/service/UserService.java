package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.repository.RoleRepository;
import com.lakindu.bangerandcobackend.repository.UserRepository;
import com.lakindu.bangerandcobackend.util.FileHandler.CompressImage;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
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

    @Autowired
    public UserService(UserRepository theUserRepository, RoleRepository theRoleRepository, Validator validator) {
        this.theUserRepository = theUserRepository;
        this.theRoleRepository = theRoleRepository;
        this.validator = validator;
    }

    public void createUser(User theNewUser, MultipartFile profilePicture) throws Exception {
        //method used to create a User
        DataBinder theDataBinder = new DataBinder(theNewUser);
        theDataBinder.addValidators((org.springframework.validation.Validator) validator);
        theDataBinder.validate();

        final BindingResult theBindingResult = theDataBinder.getBindingResult();

        if (theBindingResult.hasErrors()) {
            throw new ValidationException("Valid inputs were not provided for the fields during Sign Up.");
        } else {
            theNewUser.setProfilePicture(profilePicture.getBytes());
            ImageHandler theCompressor = new CompressImage(); //creating template method pattern
            theCompressor.processUnhandledImage(theNewUser); //calling the template method

            Role theRole = theRoleRepository.findRoleByRoleName("customer");

            if (theRole != null) {
                theNewUser.setUserRole(theRole);
                theNewUser.setBlackListed(false);
                theUserRepository.save(theNewUser);
            } else {
                throw new UnsupportedOperationException("invalid role assignment");
            }
        }
    }
}
