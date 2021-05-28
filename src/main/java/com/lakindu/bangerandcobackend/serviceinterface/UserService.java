package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.UpdateUserDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.DataFormatException;

public interface UserService extends UserDetailsService {
    User _getUserWithImageDecompression(String username) throws DataFormatException, IOException, ResourceNotFoundException;

    User _getUserWithoutDecompression(String username);

    UserDTO getUserInformation(String username) throws Exception;

    void createUser(UserDTO theNewUser, MultipartFile profilePicture) throws Exception;

    String encodePassword(String password);

    void updateUserInformation(UpdateUserDTO userInfo) throws ResourceNotFoundException;

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
