package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.UpdateUserDTO;
import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.util.exceptionhandling.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends UserDetailsService {
    User getUserInternalMethodWithDecompression(String username) throws Exception;

    UserDTO getUserInformation(String username) throws Exception;

    void createUser(UserDTO theNewUser, MultipartFile profilePicture) throws Exception;

    String encodePassword(String password);

    void updateUserInformation(UpdateUserDTO userInfo) throws ResourceNotFoundException;

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    User getUserForInquiryReply(String username);
}
