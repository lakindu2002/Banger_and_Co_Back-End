package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends UserDetailsService {
    User getUserInformation(String username) throws Exception;

    User getUserInformationWithoutImageDecompression(String username) throws Exception;

    User createUser(UserDTO theNewUser, MultipartFile profilePicture) throws Exception;

    String encodePassword(String password);

    void updateUserInformation(User userInfo);

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
