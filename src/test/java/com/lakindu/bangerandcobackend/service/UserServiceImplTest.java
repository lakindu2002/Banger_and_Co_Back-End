package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.UserDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

import javax.transaction.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private CreationUtil creationUtil;
    private List<User> createdUsers;
    private User customerToQuery;
    private Logger LOGGER = Logger.getLogger(UserServiceImplTest.class.getName());


    @BeforeEach
    void setUp() throws IOException {
        createdUsers = creationUtil.createUsersAndRoles();
        customerToQuery = createdUsers.get(0); //customer
    }

    @AfterEach
    void tearDown() {
        creationUtil.deleteRolesAndUsers();
        customerToQuery = null;
    }

    @Test
    void testShouldGetUserWithImageDecompression() {
        try {
            User user = userService._getUserWithImageDecompression(customerToQuery.getUsername());
            assertThat(user.getUsername()).isEqualTo(customerToQuery.getUsername());
            LOGGER.info("testShouldGetUserWithImageDecompression: PASSED");
        } catch (Exception ex) {
            fail("testShouldGetUserWithImageDecompression: FAILED");
        }
    }

    @Test
    void testShouldNotGetUserWithImageDecompressionWhenUsernameIsInvalid() {
        assertThrows(ResourceNotFoundException.class, () -> userService._getUserWithImageDecompression("testUserS"));
        LOGGER.info("testShouldNotGetUserWithImageDecompressionWhenUsernameIsInvalid: PASSED");
    }

    @Test
    void testShouldGetUserWithoutImageDecompression() {
        User user = userService._getUserWithoutDecompression(customerToQuery.getUsername());
        assertThat(user.getUsername()).isEqualTo(customerToQuery.getUsername());
        LOGGER.info("testShouldGetUserWithoutImageDecompression: PASSED");
    }

    @Test
    void testShouldNotGetUserWithoutImageCompressionWhenUsernameIsInvalid() {
        User testS = userService._getUserWithoutDecompression("testS");
        assertThat(testS).isNull();
        LOGGER.info("testShouldNotGetUserWithoutImageCompressionWhenUsernameIsInvalid: PASSED");
    }

    @Test
    void testShouldGetUserRole() {
        String username = customerToQuery.getUsername();
        String expectedRole = customerToQuery.getUserRole().getRoleName();
        String theRole = userService._getUserRole(username);
        assertThat(theRole).isEqualTo(expectedRole);
        LOGGER.info("testShouldGetUserRole: PASSED");
    }

    @Test
    void testShouldGetUserInformation() {
        try {
            String username = customerToQuery.getUsername();
            UserDTO userInformation = userService.getUserInformation(username);
            assertThat(userInformation).isNotNull();
            LOGGER.info("testShouldGetUserInformation: PASSED");
        } catch (Exception e) {
            fail("testShouldGetUserInformation: FAILED");
        }
    }

    @Test
    void testShouldThrowExceptionWhenUsernameIsInvalid() {
        String username = "test";
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserInformation(username));
        LOGGER.info("testShouldThrowExceptionWhenUsernameIsInvalid: PASSED");
    }

    @Test
    void testShouldGetAllCustomers() {
        try {
            List<UserDTO> allCustomers = userService.getAllCustomers();
            assertThat(allCustomers.size()).isEqualTo(2);
            LOGGER.info("testShouldGetAllCustomers: PASSED");
        } catch (Exception ex) {
            fail("testShouldGetAllCustomers: FAILED");
        }
    }

    @Test
    void testShouldGetCustomerLicenseImage() {
        String customerUsername = customerToQuery.getUsername();
        assertDoesNotThrow(() -> userService.getCustomerLicenseImage(customerUsername));
        LOGGER.info("testShouldGetCustomerLicenseImage: PASSED");
    }

    @Test
    void testShouldGetCustomerOtherIdentificationImage() {
        String customerUsername = customerToQuery.getUsername();
        assertDoesNotThrow(() -> userService.getCustomerOtherImage(customerUsername));
        LOGGER.info("testShouldGetCustomerOtherIdentificationImage: PASSED");
    }

    @Test
    void testShouldGetAllAdministrators() {
        try {
            List<UserDTO> allAdministrators = userService.getAllAdministrators();
            assertThat(allAdministrators).hasSize(1);
            LOGGER.info("testShouldGetAllAdministrators: PASSED");
        } catch (Exception e) {
            fail("testShouldGetAllAdministrators: FAILED");
        }
    }

    @Test
    void testShouldGetAllAdminEmails() {
        try {
            List<String> adminEmails = userService._getAllAdminEmails();
            assertThat(adminEmails).hasSize(1);
            LOGGER.info("testShouldGetAllAdminEmails: PASSED");
        } catch (Exception ex) {
            fail("testShouldGetAllAdminEmails: FAILED");
        }
    }
}