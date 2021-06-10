package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.UserUpdateDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;
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

    void createUser(UserDTO theNewUser, MultipartFile profilePicture) throws Exception;

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
}
