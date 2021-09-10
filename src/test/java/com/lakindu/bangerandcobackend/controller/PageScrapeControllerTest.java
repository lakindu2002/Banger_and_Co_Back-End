package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.dto.ScrapeDTO;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PageScrapeControllerTest {

    @LocalServerPort
    private int serverPort;
    @Autowired
    private CreationUtil creationUtil;

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    private final String ADMIN_USERNAME = "jadensmith";
    private final String ADMIN_PASSWORD = "test123";
    private String token;

    private final Logger LOGGER = Logger.getLogger(PageScrapeControllerTest.class.getName());

    @BeforeAll
    void beforeAll() throws IOException {
        creationUtil.createUsersAndRoles();
        loginAsAdministrator();
    }

    @AfterAll
    void afterAll() {
        creationUtil.deleteRolesAndUsers();
    }

    @Test
    void testShouldScrapePricesFromMalkeySuccessfully() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ResponseEntity<List<ScrapeDTO>> exchange = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "scrape/prices"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ScrapeDTO>>() {
                }
        );

        if (exchange.getStatusCode() != HttpStatus.OK) {
            fail("testShouldScrapePricesFromMalkeySuccessfully: FAILED");
        } else {
            assertThat(exchange.getBody().size()).isGreaterThan(0);
            LOGGER.info("testShouldScrapePricesFromMalkeySuccessfully: PASSED");
        }
    }

    private void loginAsAdministrator() {
        AuthRequest request = new AuthRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        ResponseEntity<HashMap<String, Object>> authResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "auth/login"),
                HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<HashMap<String, Object>>() {
                }
        );

        token = authResponse.getHeaders().get("Authorization").get(0);
    }
}