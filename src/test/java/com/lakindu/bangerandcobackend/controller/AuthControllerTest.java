package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @LocalServerPort
    private int serverPort;
    @Autowired
    private CreationUtil creationUtil;

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    private final String ADMIN_USERNAME = "jadensmith";
    private final String ADMIN_PASSWORD = "test123";
    private final String CUSTOMER_USERNAME = "johndoe";
    private final String CUSTOMER_PASSWORD = "test123";

    private final Logger LOGGER = Logger.getLogger(AuthControllerTest.class.getName());

    @BeforeEach
    void setUp() throws IOException {
        creationUtil.createUsersAndRoles();
    }

    @AfterEach
    void tearDown() {
        creationUtil.deleteRolesAndUsers();
    }

    @Test
    void testShouldLoginAsCustomerSuccessfully() {
        AuthRequest request = new AuthRequest();
        request.setUsername(CUSTOMER_USERNAME);
        request.setPassword(CUSTOMER_PASSWORD);

        ResponseEntity<HashMap<String, Object>> authResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "auth/login"),
                HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<HashMap<String, Object>>() {
                }
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        LOGGER.info("testShouldLoginAsCustomerSuccessfully: PASSED");
    }

    @Test
    void testShouldLoginAsAdministratorSuccessfully() {
        AuthRequest request = new AuthRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        ResponseEntity<HashMap<String, Object>> authResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "auth/login"),
                HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<HashMap<String, Object>>() {
                }
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        LOGGER.info("testShouldLoginAsAdministratorSuccessfully: PASSED");
    }

    @Test
    void testShouldNotLoginWhenUsernameIsLessThan6Characters() {
        AuthRequest request = new AuthRequest();
        request.setUsername("test");
        request.setPassword(ADMIN_PASSWORD);

        ResponseEntity<HashMap<String, Object>> authResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "auth/login"),
                HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<HashMap<String, Object>>() {
                }
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotLoginWhenUsernameIsLessThan6Characters: PASSED");
    }

    @Test
    void testShouldNotLoginWhenUsernameIsGreaterThan15Characters() {
        AuthRequest request = new AuthRequest();
        request.setUsername("TESTTESTTESTTESTTESTTEST");
        request.setPassword(ADMIN_PASSWORD);

        ResponseEntity<HashMap<String, Object>> authResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "auth/login"),
                HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<HashMap<String, Object>>() {
                }
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotLoginWhenUsernameIsGreaterThan15Characters: PASSED");
    }

    @Test
    void testShouldNotLoginWhenPasswordIsLessThan6Characters() {
        AuthRequest request = new AuthRequest();
        request.setUsername(CUSTOMER_USERNAME);
        request.setPassword("test");

        ResponseEntity<HashMap<String, Object>> authResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "auth/login"),
                HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<HashMap<String, Object>>() {
                }
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotLoginWhenPasswordIsLessThan6Characters: PASSED");
    }

    @Test
    void testShouldNotLoginWhenPasswordIsGreaterThan15Characters() {
        AuthRequest request = new AuthRequest();
        request.setUsername(CUSTOMER_USERNAME);
        request.setPassword("testtesttesttesttesttesttesttest");

        ResponseEntity<HashMap<String, Object>> authResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "auth/login"),
                HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<HashMap<String, Object>>() {
                }
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotLoginWhenPasswordIsGreaterThan15Characters: PASSED");
    }

    @Test
    void testShouldNotLoginWhenUsernameOrPasswordIsInvalid() {
        AuthRequest request = new AuthRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword("jaden123");

        ResponseEntity<HashMap<String, Object>> authResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "auth/login"),
                HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<HashMap<String, Object>>() {
                }
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        LOGGER.info("testShouldNotLoginWhenUsernameOrPasswordIsInvalid: PASSED");
    }
}