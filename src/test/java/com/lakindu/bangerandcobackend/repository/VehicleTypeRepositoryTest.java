package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.VehicleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VehicleTypeRepositoryTest {

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;
    private VehicleType vehicleType;
    private Logger LOGGER = Logger.getLogger(VehicleTypeRepositoryTest.class.getName());

    @BeforeEach
    void setUp() {
        VehicleType type = new VehicleType();
        type.setTypeName("Town Cars");
        type.setSize("Small");
        type.setVehicleList(new ArrayList<>());
        type.setPricePerDay(350);

        vehicleType = vehicleTypeRepository.save(type);
    }

    @AfterEach
    void tearDown() {
        vehicleType = null;
        vehicleTypeRepository.deleteAll();
    }

    @Test
    void testShouldFindVehicleTypeByTypeNameAndTypeSize() {
        String size = vehicleType.getSize();
        String typeName = vehicleType.getTypeName();
        VehicleType theType = vehicleTypeRepository.findVehicleTypeByTypeNameEqualsAndSizeEquals(typeName, size);
        assertThat(theType).isNotNull();
        LOGGER.info("testShouldFindVehicleTypeByTypeNameAndTypeSize: PASSED");
    }
}