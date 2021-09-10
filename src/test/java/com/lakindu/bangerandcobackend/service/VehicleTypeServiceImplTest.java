package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.VehicleTypeDTO;
import com.lakindu.bangerandcobackend.entity.VehicleType;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleTypeService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VehicleTypeServiceImplTest {

    @Autowired
    private VehicleTypeService vehicleTypeService;
    @Autowired
    private CreationUtil creationUtil;
    private List<VehicleType> typeList = new ArrayList<>();
    private VehicleType typeToDelete;
    private VehicleType typeToViewAndUpdate;
    private Logger LOGGER = Logger.getLogger(VehicleTypeServiceImplTest.class.getName());

    @BeforeEach
    void setUp() {
        typeList = creationUtil.createVehicleTypes();
        typeToDelete = typeList.get(0);
        typeToViewAndUpdate = typeList.get(1);
    }

    @AfterEach
    void tearDown() {
        typeList.clear();
        creationUtil.removeVehicleTypes();
        typeToDelete = null;
        typeToViewAndUpdate = null;
    }

    @Test
    void testShouldCreateAVehicleTypeSuccessfully() {
        VehicleTypeDTO createDTO = new VehicleTypeDTO();
        createDTO.setPricePerDay("500");
        createDTO.setSize("Large");
        createDTO.setTypeName("Town Cars");

        assertDoesNotThrow(() -> vehicleTypeService.createVehicleType(createDTO));
        LOGGER.info("testShouldCreateAVehicleTypeSuccessfully: PASSED");
    }

    @Test
    void testShouldNotCreateAVehicleTypeWithSameSizeAndName() {
        String size = typeToViewAndUpdate.getSize();
        String name = typeToViewAndUpdate.getTypeName();

        VehicleTypeDTO createDTO = new VehicleTypeDTO();
        createDTO.setPricePerDay("500");
        createDTO.setSize(size);
        createDTO.setTypeName(name);

        assertThrows(ResourceAlreadyExistsException.class, () -> vehicleTypeService.createVehicleType(createDTO));
        LOGGER.info("testShouldNotCreateAVehicleTypeWithSameSizeAndName: PASSED");
    }

    @Test
    void testShouldCreateAVehicleTypeWithDifferentSizeButSameName() {
        String size = "Medium";
        String name = typeToViewAndUpdate.getTypeName();

        VehicleTypeDTO createDTO = new VehicleTypeDTO();
        createDTO.setPricePerDay("500");
        createDTO.setSize(size);
        createDTO.setTypeName(name);

        assertDoesNotThrow(() -> vehicleTypeService.createVehicleType(createDTO));
        LOGGER.info("testShouldCreateAVehicleTypeWithDifferentSizeButSameName: PASSED");
    }

    @Test
    void testShouldCreateAVehicleTypeWithDifferentNameButSameSize() {
        String size = typeToViewAndUpdate.getSize();
        String name = "Vans";

        VehicleTypeDTO createDTO = new VehicleTypeDTO();
        createDTO.setPricePerDay("500");
        createDTO.setSize(size);
        createDTO.setTypeName(name);

        assertDoesNotThrow(() -> vehicleTypeService.createVehicleType(createDTO));
        LOGGER.info("testShouldCreateAVehicleTypeWithDifferentNameButSameSize: PASSED");
    }

    @Test
    void testShouldNotCreateATypeWhenTypeNameIsInUpperCase() {
        String size = typeToViewAndUpdate.getSize().toUpperCase(Locale.ROOT);
        String name = typeToViewAndUpdate.getTypeName().toUpperCase(Locale.ROOT);

        VehicleTypeDTO createDTO = new VehicleTypeDTO();
        createDTO.setPricePerDay("500");
        createDTO.setSize(size);
        createDTO.setTypeName(name);

        assertThrows(ResourceAlreadyExistsException.class, () -> vehicleTypeService.createVehicleType(createDTO));
        LOGGER.info("testShouldNotCreateATypeWhenTypeNameIsInUpperCase: PASSED");
    }

    @Test
    void testShouldNotCreateACreateWhenUpperCaseIsRandom() {
        String size = "SmALl";
        String name = "ToWN CarS";

        VehicleTypeDTO createDTO = new VehicleTypeDTO();
        createDTO.setPricePerDay("500");
        createDTO.setSize(size);
        createDTO.setTypeName(name);

        assertThrows(ResourceAlreadyExistsException.class, () -> vehicleTypeService.createVehicleType(createDTO));
        LOGGER.info("testShouldNotCreateACreateWhenUpperCaseIsRandom: PASSED");
    }

    @Test
    void testShouldGetAllVehicleTypes() {
        List<VehicleTypeDTO> allVehicleTypes = vehicleTypeService.getAllVehicleTypes();
        assertThat(allVehicleTypes.size()).isGreaterThan(0);
        LOGGER.info("testShouldGetAllVehicleTypes: PASSED");
    }

    @Test
    void testShouldFindAVehicleTypeById() {
        try {
            int idToGet = typeToViewAndUpdate.getVehicleTypeId();
            VehicleTypeDTO vehicleTypeById = vehicleTypeService.findVehicleTypeById(idToGet);
            assertThat(vehicleTypeById.getVehicleTypeId()).isEqualTo(idToGet);
            LOGGER.info("testShouldFindAVehicleTypeById: PASSED");
        } catch (Exception e) {
            fail("testShouldFindAVehicleTypeById: FAILED");
        }
    }

    @Test
    void testShouldNotFindAVehicleTypeByIdWhenIdIsInvalid() {
        int idToGet = 1000;
        assertThrows(ResourceNotFoundException.class, () -> vehicleTypeService.findVehicleTypeById(idToGet));
        LOGGER.info("testShouldNotFindAVehicleTypeByIdWhenIdIsInvalid: PASSED");
    }

    @Test
    void testShouldGetVehicleTypeByCallingInternalMethod() {
        try {
            int idToGet = typeToViewAndUpdate.getVehicleTypeId();
            VehicleType vehicleType = vehicleTypeService._getType(idToGet);
            assertThat(vehicleType.getVehicleTypeId()).isEqualTo(idToGet);
            LOGGER.info("testShouldGetVehicleTypeByCallingInternalMethod: PASSED");
        } catch (Exception e) {
            fail("testShouldGetVehicleTypeByCallingInternalMethod: FAILED");
        }
    }

    @Test
    void testShouldNotGetVehicleTypeByCallingInternalMethodWhenIdIsInvalid() {
        int idToGet = 1000;
        assertThrows(ResourceNotFoundException.class, () -> vehicleTypeService._getType(idToGet));
        LOGGER.info("testShouldNotGetVehicleTypeByCallingInternalMethodWhenIdIsInvalid: PASSED");
    }

    @Test
    void testShouldConvertEntityToDTO() {
        VehicleTypeDTO vehicleTypeDTO = vehicleTypeService.constructDTO(typeToViewAndUpdate);
        assertThat(vehicleTypeDTO.getVehicleTypeId()).isEqualTo(typeToViewAndUpdate.getVehicleTypeId());
        LOGGER.info("testShouldConvertEntityToDTO: PASSED");
    }

    @Test
    void testShouldRemoveAVehicleTypeSuccessfully() {
        assertDoesNotThrow(() -> vehicleTypeService.removeVehicleType(typeToDelete.getVehicleTypeId()));
        LOGGER.info("testShouldRemoveAVehicleTypeSuccessfully: PASSED");
    }

    @Test
    void testShouldNotRemoveATypeWhenIdIsInvalid() {
        assertThrows(ResourceNotFoundException.class, () -> vehicleTypeService.removeVehicleType(10000));
        LOGGER.info("testShouldNotRemoveATypeWhenIdIsInvalid: PASSED");
    }
}