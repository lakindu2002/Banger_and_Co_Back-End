package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;
    private Vehicle createdVehicle;
    private Logger LOGGER = Logger.getLogger(VehicleRepositoryTest.class.getName());

    @BeforeEach
    void setUp() {
        VehicleType type = new VehicleType();
        type.setTypeName("Town Cars");
        type.setSize("Small");
        type.setVehicleList(new ArrayList<>());
        type.setPricePerDay(350);

        VehicleType createdType = vehicleTypeRepository.save(type);

        Vehicle theVehicle = new Vehicle();
        theVehicle.setVehicleImage(new byte[]{});
        theVehicle.setTheVehicleType(createdType);
        theVehicle.setVehicleName("Mercedes Benz");
        theVehicle.setRentalsForTheVehicle(new ArrayList<>());
        theVehicle.setFuelType("Petrol");
        theVehicle.setLicensePlate("CSA-7895");
        theVehicle.setSeatingCapacity(8);
        theVehicle.setTransmission("Automatic");

        createdVehicle = vehicleRepository.save(theVehicle);
    }

    @AfterEach
    void tearDown() {
        createdVehicle = null;
        vehicleRepository.deleteAll();
        vehicleTypeRepository.deleteAll();
    }

    @Test
    void testShouldGetVehicleByLicensePlate() {
        String licensePlate = "CSA-7895";
        Vehicle vehicleByLicensePlateEquals = vehicleRepository.getVehicleByLicensePlateEquals(licensePlate);
        assertThat(vehicleByLicensePlateEquals).isNotNull();
        LOGGER.info("testShouldGetVehicleByLicensePlate: PASSED");
    }

    @Test
    void testShouldNotGetVehicleByLicensePlateWhenLicensePlateIsInvalid() {
        String licensePlate = "CAJ-7895";
        Vehicle vehicleByLicensePlateEquals = vehicleRepository.getVehicleByLicensePlateEquals(licensePlate);
        assertThat(vehicleByLicensePlateEquals).isNull();
        LOGGER.info("testShouldNotGetVehicleByLicensePlateWhenLicensePlateIsInvalid: PASSED");
    }
}