package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AdditionalEquipmentServiceImplTest {

    @Autowired
    private CreationUtil creationUtil;

    @Autowired
    @Qualifier("additionalEquipmentServiceImpl")
    private AdditionalEquipmentService additionalEquipmentService;
    private AdditionalEquipment equipmentToRemove;
    private AdditionalEquipment equipmentToGet;
    private Logger LOGGER = Logger.getLogger(AdditionalEquipmentServiceImplTest.class.getName());

    private List<AdditionalEquipment> createdEquipments = new ArrayList<>();

    @BeforeAll
    void beforeAll() {
        createdEquipments = creationUtil.createAdditionalEquipments();
        equipmentToRemove = createdEquipments.get(0);
        equipmentToGet = createdEquipments.get(1);
    }

    @AfterAll
    void afterAll() {
        creationUtil.removeAllAdditionalEquipments();
        creationUtil = null;
    }

    @Test
    void testShouldGetAllAdditionalEquipment() {
        List<AdditionalEquipmentDTO> allAdditionalEquipment = additionalEquipmentService.getAllAdditionalEquipment();
        assertThat(allAdditionalEquipment.size()).isEqualTo(createdEquipments.size());
        LOGGER.info("testShouldGetAllAdditionalEquipment: PASSED");
    }

    @Test
    void testShouldGetAllAvailableAdditionalEquipment() {
        //1 out of 3 with no quantity is creating in BeforeEach.
        List<AdditionalEquipmentDTO> allAvailable = additionalEquipmentService.getAllAvailableAdditionalEquipment();
        assertThat(allAvailable.size()).isEqualTo(2);
        LOGGER.info("testShouldGetAllAvailableAdditionalEquipment: PASSED");
    }

    @Test
    void testShouldCreateAnAdditionalEquipmentSuccessfully() {
        AdditionalEquipmentDTO createDTO = new AdditionalEquipmentDTO();
        createDTO.setEquipmentName("Bottle Holder");
        createDTO.setEquipmentQuantity(30);
        createDTO.setPricePerDay("750.00");

        assertDoesNotThrow(() -> additionalEquipmentService.createAdditionalEquipment(createDTO));
        LOGGER.info("testShouldCreateAnAdditionalEquipmentSuccessfully: PASSED");
    }

    @Test
    void testShouldNotCreateEquipmentWithDuplicateName() {
        AdditionalEquipmentDTO createDTO = new AdditionalEquipmentDTO();
        createDTO.setEquipmentName("Wine Chiller");
        createDTO.setEquipmentQuantity(30);
        createDTO.setPricePerDay("750.00");

        assertThrows(ResourceAlreadyExistsException.class, () -> additionalEquipmentService.createAdditionalEquipment(createDTO));
        LOGGER.info("testShouldNotCreateEquipmentWithDuplicateName: PASSED");
    }

    @Test
    void testShouldNotCreateEquipmentWithNoQuantity() {
        AdditionalEquipmentDTO createDTO = new AdditionalEquipmentDTO();
        createDTO.setEquipmentName("Wine Chiller");
        createDTO.setEquipmentQuantity(0);
        createDTO.setPricePerDay("750.00");

        assertThrows(BadValuePassedException.class, () -> additionalEquipmentService.createAdditionalEquipment(createDTO));
        LOGGER.info("testShouldNotCreateEquipmentWithNoQuantity: PASSED");
    }

    @Test
    void testShouldRemoveAnEquipmentSuccessfully() {
        assertDoesNotThrow(() -> additionalEquipmentService.removeEquipment(equipmentToRemove.getEquipmentId()));
        LOGGER.info("testShouldRemoveAnEquipmentSuccessfully: PASSED");
    }

    @Test
    void testShouldGetEquipmentById() {
        int idToGet = equipmentToGet.getEquipmentId();
        try {
            AdditionalEquipmentDTO equipmentByID = additionalEquipmentService.getEquipmentByID(idToGet);
            assertThat(equipmentByID.getEquipmentName()).isEqualTo(equipmentToGet.getEquipmentName());
            LOGGER.info("testShouldGetEquipmentById: PASSED");
        } catch (Exception e) {
            fail("testShouldGetEquipmentById: FAILED");
        }
    }

    @Test
    void testShouldGetEquipmentByIdByCallingInternalMethod() {
        int idToGet = equipmentToGet.getEquipmentId();
        try {
            AdditionalEquipment equipment = additionalEquipmentService._getAdditionalEquipmentById(idToGet);
            assertThat(equipment.getEquipmentName()).isEqualTo(equipmentToGet.getEquipmentName());
            LOGGER.info("testShouldGetEquipmentByIdByCallingInternalMethod: PASSED");
        } catch (Exception e) {
            fail("testShouldGetEquipmentByIdByCallingInternalMethod: FAILED");
        }
    }

    @Test
    void testShouldCheckIfEquipmentHasPendingOrOngoingRentals() {
        assertDoesNotThrow(() -> additionalEquipmentService.checkIfEquipmentHasPendingOrOngoingRentals(equipmentToGet));
        LOGGER.info("testShouldCheckIfEquipmentHasPendingOrOngoingRentals: PASSED");
    }
}