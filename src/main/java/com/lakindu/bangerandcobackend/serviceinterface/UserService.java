package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.UserAdminCreateDTO;
import com.lakindu.bangerandcobackend.dto.UserUpdateDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.*;
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

    String _getUserRole(String userName);

    UserDTO getUserInformation(String username) throws Exception;

    void createUser(UserDTO theNewUser, MultipartFile profilePicture, MultipartFile licensePicture, MultipartFile otherIdentity) throws Exception;

    String encodePassword(String password);

    void updateUserInformation(UserUpdateDTO userInfo) throws ResourceNotFoundException, BadValuePassedException, ResourceAlreadyExistsException;

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

    /**
     * Method will return a list of all available administrators in the system.
     *
     * @return List of admins at Banger and Co.
     */
    List<UserDTO> getAllAdministrators() throws DataFormatException, IOException;

    /**
     * Method will create an administrator account at Banger And Co.
     *
     * @param createdDTO The administrator to be created.
     */
    void createAdmin(UserAdminCreateDTO createdDTO) throws ResourceAlreadyExistsException, DataFormatException, IOException, ResourceNotFoundException, ResourceNotCreatedException;

    /**
     * Method executed by the administrator to remove their account. They cannot remove other admin accounts, only their own.
     * <p>
     * There must be a minimum of one administrators present in the system at any given time.
     * </p>
     *
     * @param username     The administrator to remove the account for
     * @param loggedInUser The logged in user in the spring security context
     * @throws ResourceCannotBeDeletedException The exception thrown when the user cannot be removed.
     */
    void removeAdministrator(String username, Authentication loggedInUser) throws ResourceCannotBeDeletedException, ResourceNotFoundException;

    /**
     * Method will change the user status to blacklisted = true and will email the user to inform that they have been blacklisted.
     *
     * @param usernameOfCustomer    The customer going to be blacklisted
     * @param theRentalNotCollected The rental that they did not collect
     */
    void blackListCustomer(String usernameOfCustomer, Rental theRentalNotCollected);

    List<String> _getAllAdminEmails() throws ResourceNotFoundException;
}
