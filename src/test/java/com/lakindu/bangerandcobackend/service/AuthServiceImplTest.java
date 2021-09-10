package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.auth.AuthReturnBuilder;
import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.AuthService;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceImplTest {
    @Autowired
    private CreationUtil creationUtil;
    @Autowired
    @Qualifier("authServiceImpl")
    private AuthService authService;

    private List<User> createdUsers = new ArrayList<>();
    private String adminUsername = "jadensmith";
    private String customerUsername = "johndoe";

    private Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());

    @BeforeEach
    void setUp() throws IOException {
        createdUsers = creationUtil.createUsersAndRoles();
    }

    @AfterEach
    void tearDown() {
        createdUsers.clear();
        creationUtil.deleteRolesAndUsers();
    }

    @Test
    void testShouldLoginAsCustomerSuccessfully() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(customerUsername);
        authRequest.setPassword("test123");

        try {
            AuthReturnBuilder authReturnBuilder = authService.performAuthentication(authRequest);
            assertThat(authReturnBuilder.getUserDTO().getUserRole()).isEqualTo("customer");
            LOGGER.info("testShouldLoginAsCustomerSuccessfully: PASSED");
        } catch (Exception e) {
            fail("testShouldLoginAsCustomerSuccessfully: FAILED");
        }
    }

    @Test
    void testShouldLoginAsAdministratorSuccessfully() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(adminUsername);
        authRequest.setPassword("test123");

        try {
            AuthReturnBuilder authReturnBuilder = authService.performAuthentication(authRequest);
            assertThat(authReturnBuilder.getUserDTO().getUserRole()).isEqualTo("administrator");
            LOGGER.info("testShouldLoginAsAdministratorSuccessfully: PASSED");
        } catch (Exception e) {
            fail("testShouldLoginAsCustomerSuccessfully: FAILED");
        }
    }

    @Test
    void testShouldNotLoginWithInvalidUsername() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("john123");
        authRequest.setPassword("test123");

        assertThrows(BadCredentialsException.class, () -> authService.performAuthentication(authRequest));
        LOGGER.info("testShouldNotLoginWithInvalidUsername: PASSED");
    }

    @Test
    void testShouldNotLoginWithInvalidPassword() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(customerUsername);
        authRequest.setPassword("TEST123");

        assertThrows(BadCredentialsException.class, () -> authService.performAuthentication(authRequest));
        LOGGER.info("testShouldNotLoginWithInvalidPassword: PASSED");
    }

    @Test
    void testShouldNotLoginWithAccountThatDoesNotExist() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("customerUsername");
        authRequest.setPassword("TEST123");

        assertThrows(BadCredentialsException.class, () -> authService.performAuthentication(authRequest));
        LOGGER.info("testShouldNotLoginWithAccountThatDoesNotExist: PASSED");
    }
}