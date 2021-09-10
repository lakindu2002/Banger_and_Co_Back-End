package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.VehicleCreateDTO;
import com.lakindu.bangerandcobackend.dto.VehicleShowDTO;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VehicleServiceImplTest {

    @Autowired
    private CreationUtil creationUtil;
    @Autowired
    private VehicleService vehicleService;
    private List<Vehicle> createdVehicles;
    private List<VehicleType> vehicleTypes;

    private Vehicle vehicleToView;
    private Vehicle vehicleToDelete;
    private VehicleType createType;

    private Logger LOGGER = Logger.getLogger(VehicleTypeServiceImplTest.class.getName());

    @BeforeEach
    void setUp() throws IOException {
        vehicleTypes = creationUtil.createVehicleTypes();
        createdVehicles = creationUtil.createVehicles(vehicleTypes);
        vehicleToView = createdVehicles.get(0);
        vehicleToDelete = createdVehicles.get(1);
        createType = vehicleTypes.get(0);
    }

    @AfterEach
    void tearDown() {
        creationUtil.deleteVehicles();
        vehicleTypes.clear();
        vehicleToView = null;
        createType = null;
        vehicleToDelete = null;
    }

    @Test
    void testShouldCreateAVehicleSuccessfully() {
        VehicleCreateDTO theDTO = new VehicleCreateDTO();
        theDTO.setFuelType("Petrol");
        theDTO.setVehicleName("Tesla");
        theDTO.setLicensePlate("XAJ-1234");
        theDTO.setVehicleTypeId(createType.getVehicleTypeId());
        theDTO.setTransmission("Manual");
        theDTO.setSeatingCapacity(4);

        MultipartFile theFile = new MockMultipartFile("Test", new byte[]{});

        assertDoesNotThrow(() -> vehicleService.createVehicle(theDTO, theFile));
    }

    @Test
    void testShouldNotCreateVehicleWhenLicensePlateExists() {
        String plate = vehicleToView.getLicensePlate();
        VehicleCreateDTO theDTO = new VehicleCreateDTO();
        theDTO.setFuelType("Petrol");
        theDTO.setVehicleName("Tesla");
        theDTO.setLicensePlate(plate);
        theDTO.setVehicleTypeId(createType.getVehicleTypeId());
        theDTO.setTransmission("Manual");
        theDTO.setSeatingCapacity(4);

        MultipartFile theFile = new MockMultipartFile("Test", new byte[]{});

        assertThrows(ResourceAlreadyExistsException.class, () -> vehicleService.createVehicle(theDTO, theFile));
    }

    @Test
    void testShouldNotCreateAVehicleWhenVehicleTypeIsInvalid() {
        VehicleCreateDTO theDTO = new VehicleCreateDTO();
        theDTO.setFuelType("Petrol");
        theDTO.setVehicleName("Tesla");
        theDTO.setLicensePlate("XAJ-1234");
        theDTO.setVehicleTypeId(10000);
        theDTO.setTransmission("Manual");
        theDTO.setSeatingCapacity(4);

        MultipartFile theFile = new MockMultipartFile("Test", new byte[]{});

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.createVehicle(theDTO, theFile));
    }

    @Test
    void testShouldGetVehicleInformationOnInternalMethod() {
        try {
            int id = vehicleToView.getVehicleId();
            Vehicle vehicle = vehicleService._getVehicleInformation(id);
            assertThat(vehicle.getVehicleId()).isEqualTo(id);
        } catch (Exception e) {
            fail("testShouldGetVehicleInformationOnInternalMethod: FAILED");
        }
    }

    @Test
    void testShouldNotGetVehicleWhenVehicleIdDoesNotExist() {
        int id = 1000;
        assertThrows(ResourceNotFoundException.class, () -> vehicleService._getVehicleInformation(id));
    }

    @Test
    void testShouldNotRemoveAVehicleWhenIdIsInvalid() {
        int id = 1000;
        assertThrows(ResourceNotFoundException.class, () -> vehicleService.removeVehicleById(id));
    }

    @Test
    void testWillCheckIsVehicleHasPendingOrOnGoingRentals() {
        assertDoesNotThrow(() -> vehicleService.checkIfVehicleHasPendingOrOnGoingRentals(vehicleToDelete));
    }


    @Test
    void testShouldRemoveAVehicleSuccessfully() {
        int id = vehicleToDelete.getVehicleId();
        assertDoesNotThrow(() -> vehicleService.removeVehicleById(id));
    }

    @Test
    void testShouldGetAVehicleByIdSuccessfully() {
        int id = vehicleToView.getVehicleId();
        try {
            VehicleShowDTO vehicleById = vehicleService.getVehicleById(id);
            assertThat(vehicleById.getVehicleId()).isEqualTo(id);
        } catch (Exception ex) {
            fail("testShouldGetAVehicleByIdSuccessfully: FAILED");
        }
    }

    @Test
    void testShouldNotGetVehicleByIdWhenIdIsInvalid() {
        int id = 1000;
        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getVehicleById(id));
    }
}