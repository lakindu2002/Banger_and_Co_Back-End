package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AdditionalEquipmentRepositoryTest {

    @Autowired
    private AdditionalEquipmentRepository additionalEquipmentRepository;
    private AdditionalEquipment createdEquipment;
    private Logger LOGGER = Logger.getLogger(AdditionalEquipmentRepositoryTest.class.getName());

    @BeforeEach
    void setUp() {
        AdditionalEquipment equipment = new AdditionalEquipment();
        equipment.setEquipmentQuantity(30);
        equipment.setEquipmentName("Sat Nav");
        equipment.setPricePerDay(350);
        equipment.setRentalsHavingThisCustomization(new ArrayList<>());

        createdEquipment = additionalEquipmentRepository.save(equipment);
    }

    @AfterEach
    void tearDown() {
        createdEquipment = null;
        additionalEquipmentRepository.deleteAll();
    }

    @Test
    void testShouldFindAdditionalEquipmentByEquipmentName() {
        String nameToGet = createdEquipment.getEquipmentName();
        Optional<AdditionalEquipment> theEquipment = additionalEquipmentRepository.findAdditionalEquipmentByEquipmentName(nameToGet);
        assertThat(theEquipment.isPresent()).isTrue();

        LOGGER.info("testShouldFindAdditionalEquipmentByEquipmentName: PASSED");
    }

    @Test
    void testShouldGetEquipmentWithNameOnSeperateEquipmentId() {
        String nameToGet = createdEquipment.getEquipmentName();
        int idNotToEqual = createdEquipment.getEquipmentId();

        AdditionalEquipment theEquipment = additionalEquipmentRepository.getItemWithSameNameButAsASeperateEntry(nameToGet, idNotToEqual);
        assertThat(theEquipment).isNull();

        LOGGER.info("testShouldGetEquipmentWithNameOnSeperateEquipmentId: PASSED");
    }

    @Test
    void testShouldGetAllAvailableAdditionalEquipments() {
        List<AdditionalEquipment> allAvailableEquipments = additionalEquipmentRepository.getAllAvailableEquipments();
        assertThat(allAvailableEquipments.size()).isEqualTo(1);

        LOGGER.info("testShouldGetAllAvailableAdditionalEquipments: PASSED");
    }
}