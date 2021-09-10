package com.lakindu.bangerandcobackend.controller;

import com.lakindu.bangerandcobackend.dto.AuthRequest;
import com.lakindu.bangerandcobackend.dto.VehicleTypeDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.util.exceptionhandling.BangerAndCoResponse;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class VehicleTypeControllerTest {

    @LocalServerPort
    private int serverPort;
    @Autowired
    private CreationUtil creationUtil;
    private VehicleType typeToView;
    private VehicleType typeToDelete;

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    private final String ADMIN_USERNAME = "jadensmith";
    private final String ADMIN_PASSWORD = "test123";
    private String token;
    private final Logger LOGGER = Logger.getLogger(VehicleTypeControllerTest.class.getName());


    @BeforeEach
    void setUp() throws IOException {
        List<User> usersAndRoles = creationUtil.createUsersAndRoles();
        List<VehicleType> vehicleTypes = creationUtil.createVehicleTypes();

        typeToView = vehicleTypes.get(0);
        typeToDelete = vehicleTypes.get(1);

        loginAsAdministrator();
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

    @AfterEach
    void tearDown() {
        creationUtil.deleteRolesAndUsers();
        creationUtil.removeVehicleTypes();
    }

    @Test
    void testShouldCreateAVehicleTypeSuccessfully() {
        VehicleTypeDTO typeDto = new VehicleTypeDTO();
        typeDto.setSize("Small");
        typeDto.setTypeName("Caravan");
        typeDto.setPricePerDay("500");

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/create"),
                HttpMethod.POST, new HttpEntity<>(typeDto, header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.OK);
        LOGGER.info("testShouldCreateAVehicleTypeSuccessfully: PASSED");
    }

    @Test
    void testShouldNotCreateATypeWithSameNameAndSameSize() {
        VehicleTypeDTO typeDto = new VehicleTypeDTO();
        typeDto.setSize("Small");
        typeDto.setTypeName("Town Cars");
        typeDto.setPricePerDay("500");

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/create"),
                HttpMethod.POST, new HttpEntity<>(typeDto, header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        LOGGER.info("testShouldNotCreateATypeWithSameNameAndSameSize: PASSED");
    }

    @Test
    void testShouldCreateTypeSuccessfullyWhenNameIsSameButSizeIsDifferent() {
        VehicleTypeDTO typeDto = new VehicleTypeDTO();
        typeDto.setSize("large");
        typeDto.setTypeName("Town Cars");
        typeDto.setPricePerDay("500");

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/create"),
                HttpMethod.POST, new HttpEntity<>(typeDto, header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.OK);
        LOGGER.info("testShouldCreateTypeSuccessfullyWhenNameIsSameButSizeIsDifferent: PASSED");
    }

    @Test
    void testShouldNotCreateTypeWhenTypeNameAndSizeIsSameInCaseInSensitive() {
        VehicleTypeDTO typeDto = new VehicleTypeDTO();
        typeDto.setSize("SmALl");
        typeDto.setTypeName("TOwN CaRs");
        typeDto.setPricePerDay("500");

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/create"),
                HttpMethod.POST, new HttpEntity<>(typeDto, header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        LOGGER.info("testShouldNotCreateTypeWhenTypeNameAndSizeIsSameInCaseInSensitive: PASSED");
    }

    @Test
    void testShouldNotCreateTypeDueToInvalidPrice() {
        VehicleTypeDTO typeDto = new VehicleTypeDTO();
        typeDto.setSize("Small");
        typeDto.setTypeName("TOwN CaRs");
        typeDto.setPricePerDay("5000000000");

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/create"),
                HttpMethod.POST, new HttpEntity<>(typeDto, header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateTypeDueToInvalidPrice: PASSED");
    }

    @Test
    void testShouldNotCreateTypeDueToNullSizeName() {
        VehicleTypeDTO typeDto = new VehicleTypeDTO();
        typeDto.setSize(null);
        typeDto.setTypeName(null);
        typeDto.setPricePerDay("5000000000");

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/create"),
                HttpMethod.POST, new HttpEntity<>(typeDto, header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateTypeDueToNullSizeName: PASSED");
    }

    @Test
    void testShouldNotCreateTypeDueToNullPrice() {
        VehicleTypeDTO typeDto = new VehicleTypeDTO();
        typeDto.setSize("large");
        typeDto.setTypeName("town car");
        typeDto.setPricePerDay(null);

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/create"),
                HttpMethod.POST, new HttpEntity<>(typeDto, header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateTypeDueToNullPrice: PASSED");
    }

    @Test
    void testShouldNotCreateTypeDueToNullSize() {
        VehicleTypeDTO typeDto = new VehicleTypeDTO();
        typeDto.setSize(null);
        typeDto.setTypeName("town car");
        typeDto.setPricePerDay(null);

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/create"),
                HttpMethod.POST, new HttpEntity<>(typeDto, header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        LOGGER.info("testShouldNotCreateTypeDueToNullSize: PASSED");
    }

    @Test
    void testShouldGetAllVehicleTypes() {
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<List<VehicleTypeDTO>> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/findAll"),
                HttpMethod.GET, new HttpEntity<>(header), new ParameterizedTypeReference<List<VehicleTypeDTO>>() {
                }
        );

        assertThat(call.getBody().size()).isGreaterThan(0);
        LOGGER.info("testShouldGetAllVehicleTypes: PASSED");
    }

    @Test
    void testShouldFindTypeById() {
        int idToFind = typeToView.getVehicleTypeId();

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<VehicleTypeDTO> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/find/" + idToFind),
                HttpMethod.GET, new HttpEntity<>(header), VehicleTypeDTO.class
        );

        assertThat(call.getBody().getVehicleTypeId()).isEqualTo(idToFind);
        LOGGER.info("testShouldFindTypeById: PASSED");
    }

    @Test
    void testShouldRemoveVehicleTypeById() {
        int idToDelete = typeToDelete.getVehicleTypeId();

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);

        ResponseEntity<BangerAndCoResponse> call = testRestTemplate.exchange(
                creationUtil.constructAPIUrl(serverPort, "vehicleType/remove/" + idToDelete),
                HttpMethod.DELETE, new HttpEntity<>(header), BangerAndCoResponse.class
        );

        assertThat(call.getStatusCode()).isEqualTo(HttpStatus.OK);
        LOGGER.info("testShouldRemoveVehicleTypeById: PASSED");
    }
}