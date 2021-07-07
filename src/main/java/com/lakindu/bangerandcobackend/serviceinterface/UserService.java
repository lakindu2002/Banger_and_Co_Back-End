package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.UserUpdateDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

public interface UserService extends UserDetailsService {
    User _getUserWithImageDecompression(String username) throws DataFormatException, IOException, ResourceNotFoundException;

    User _getUserWithoutDecompression(String username);

    UserDTO getUserInformation(String username) throws Exception;

    void createUser(UserDTO theNewUser, MultipartFile profilePicture, MultipartFile licensePicture, MultipartFile otherIdentity) throws Exception;

    String encodePassword(String password);

    void updateUserInformation(UserUpdateDTO userInfo) throws ResourceNotFoundException, BadValuePassedException;

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    List<UserDTO> getAllCustomers() throws DataFormatException, IOException;

    /**
     * Allows the administrator to whitelist a customer that been blacklisted by the system automatically.
     *
     * @param username The user to white list.
     * @return The user that has been whitelisted and has been allowed to make rentals again.
     * @throws ResourceNotUpdatedException Thrown when the user cannot be whitelisted due to logical error
     * @throws ResourceNotFoundException   Thrown when the user does not exist in the system.
     * @author Lakindu Hewawasam
     */
    User whitelistCustomer(String username) throws ResourceNotFoundException, ResourceNotUpdatedException;

    /**
     * Method will update the customer license image in the database
     *
     * @param customerUsername The customer to update the license image for
     * @param licenseImage     The new license image for the customer
     * @param loggedInUser     Information of logged in user.
     * @author Lakindu Hewawasam
     */
    void updateCustomerLicenseImage(String customerUsername, MultipartFile licenseImage, Authentication loggedInUser) throws ResourceNotUpdatedException, IOException, DataFormatException;

    /**
     * Method will decompress the license image and return it back to the client.
     *
     * @param username The customer to get the license image for
     * @return The decompressed image
     * @throws DataFormatException Thrown when failure in Decompression
     * @throws IOException         Thrown during buffer handling
     */
    byte[] getCustomerLicenseImage(String username) throws DataFormatException, IOException;

    /**
     * Method will decompress the other identity image and return it back to the client.
     *
     * @param username The customer to get the other identity for
     * @return The decompressed image
     * @throws DataFormatException Thrown when failure in Decompression
     * @throws IOException         Thrown during buffer handling
     */
    byte[] getCustomerOtherImage(String username) throws DataFormatException, IOException;

    /**
     * Method will update the customer license image in the database
     *
     * @param customerUsername The customer to update the license image for
     * @param additionalImage  The new additional identity image for the customer
     * @param loggedInUser     The information of logged in user
     * @author Lakindu Hewawasam
     */
    void updateCustomerOtherImage(String customerUsername, MultipartFile additionalImage, Authentication loggedInUser) throws ResourceNotUpdatedException, IOException, DataFormatException;
}
