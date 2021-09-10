package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdditionalEquipmentControllerTest {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private CreationUtil creationUtil;

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    private final String ADMIN_USERNAME = "jadensmith";
    private final String ADMIN_PASSWORD = "test123";
    private String token;
    private AdditionalEquipment equipmentToView;
    private AdditionalEquipment equipmentToDelete;

    private final Logger LOGGER = Logger.getLogger(AdditionalEquipmentControllerTest.class.getName());

    @BeforeAll
    void beforeAll() throws IOException {
        creationUtil.createUsersAndRoles();
        List<AdditionalEquipment> additionalEquipments = creationUtil.createAdditionalEquipments();
        equipmentToView = additionalEquipments.get(0);
        equipmentToDelete = additionalEquipments.get(1);
        loginAsAdministrator();
    }

    @AfterAll
    void afterAll() {
        creationUtil.deleteRolesAndUsers();
        creationUtil.removeAllAdditionalEquipments();
    }

    @Test
    void testShouldCreateAdditionalEquipmentSuccessfully() {
        AdditionalEquipmentDTO dto = new AdditionalEquipmentDTO();
        dto.setEquipmentQuantity(30);
        dto.setEquipmentName("Test Equipment");
        dto.setPricePerDay("500");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> createResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "equipment/create"),
                HttpMethod.POST, new HttpEntity<>(dto, headers), BangerAndCoResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        LOGGER.info("testShouldCreateAdditionalEquipmentSuccessfully: PASSED");
    }

    @Test
    void testShouldNotCreateAdditionalEquipmentWithDuplicateName() {
        AdditionalEquipmentDTO dto = new AdditionalEquipmentDTO();
        dto.setEquipmentQuantity(30);
        dto.setEquipmentName(equipmentToView.getEquipmentName());
        dto.setPricePerDay("500");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> createResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "equipment/create"),
                HttpMethod.POST, new HttpEntity<>(dto, headers), BangerAndCoResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        LOGGER.info("testShouldNotCreateAdditionalEquipmentWithDuplicateName: PASSED");
    }

    @Test
    void testShouldNotCreateAdditionalEquipmentWithInvalidInputs() {
        AdditionalEquipmentDTO dto = new AdditionalEquipmentDTO();
        dto.setEquipmentQuantity(30);
        dto.setEquipmentName("");
        dto.setPricePerDay("500");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> createResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "equipment/create"),
                HttpMethod.POST, new HttpEntity<>(dto, headers), BangerAndCoResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateAdditionalEquipmentWithInvalidInputs: PASSED");
    }

    @Test
    void testShouldNotCreateEquipmentWithInvalidPrice() {
        AdditionalEquipmentDTO dto = new AdditionalEquipmentDTO();
        dto.setEquipmentQuantity(30);
        dto.setEquipmentName("");
        dto.setPricePerDay("50000000000000");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> createResponse = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "equipment/create"),
                HttpMethod.POST, new HttpEntity<>(dto, headers), BangerAndCoResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateEquipmentWithInvalidPrice: PASSED");
    }

    @Test
    void testShouldGetAllAdditionalEquipment() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ResponseEntity<List<AdditionalEquipmentDTO>> exchange = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "equipment/all"),
                HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<AdditionalEquipmentDTO>>() {
                }
        );
        System.out.println(exchange.getStatusCode());
        assertThat(exchange.getBody().size()).isGreaterThan(0);
        LOGGER.info("testShouldGetAllAdditionalEquipment: PASSED");
    }

    @Test
    void testShouldGetEquipmentById() {
        int id = equipmentToView.getEquipmentId();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ResponseEntity<AdditionalEquipmentDTO> exchange = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "equipment/get/" + id),
                HttpMethod.GET, new HttpEntity<>(headers), AdditionalEquipmentDTO.class
        );

        assertThat(exchange.getBody().getEquipmentId()).isEqualTo(id);
        LOGGER.info("testShouldGetEquipmentById: PASSED");
    }

    @Test
    void testShouldRemoveEquipmentById() {
        int id = equipmentToDelete.getEquipmentId();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "equipment/remove/" + id),
                HttpMethod.DELETE, new HttpEntity<>(headers), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.OK);
        LOGGER.info("testShouldRemoveEquipmentById: PASSED");

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