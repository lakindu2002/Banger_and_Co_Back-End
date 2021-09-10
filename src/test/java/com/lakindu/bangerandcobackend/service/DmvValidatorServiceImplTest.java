package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.serviceinterface.DmvValidatorService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class DmvValidatorServiceImplTest {
    @Autowired
    private CreationUtil creationUtil;
    @Autowired
    @Qualifier("dmvValidatorServiceImpl")
    private DmvValidatorService dmvValidatorService;
    private final Logger LOGGER = Logger.getLogger(DmvValidatorServiceImplTest.class.getName());

    private User customer; //dmv has this license
    private User validLicenseCustomer;

    @BeforeEach
    void setUp() throws IOException {
        List<User> createdUsers = creationUtil.createUsersAndRoles();
        customer = createdUsers.get(0);
        validLicenseCustomer = createdUsers.get(2);
    }

    @AfterEach
    void tearDown() {
        creationUtil.deleteRolesAndUsers();
        customer = null;
        validLicenseCustomer = null;
    }

    @Test
    void testShouldNotCheckCSVWhenCustomerNotPresent() {
        assertThrows(ResourceNotFoundException.class, () -> dmvValidatorService.isLicenseSuspendedLostStolen(null));
        LOGGER.info("testShouldNotCheckCSVWhenCustomerNotPresent: PASSED");
    }

    @Test
    void testShouldValidateTheUserDrivingLicense() {
        try {
            HashMap<String, String> licenseSuspendedLostStolen = dmvValidatorService.isLicenseSuspendedLostStolen(customer);
            assertThat(licenseSuspendedLostStolen.get(DmvValidatorServiceImpl.DMVType.STATUS_TYPE.value)).isEqualTo(DmvValidatorServiceImpl.DMVType.STOLEN.value);
            LOGGER.info("testShouldValidateTheUserDrivingLicense: PASSED");
        } catch (IOException | ResourceNotFoundException e) {
            fail("testShouldValidateTheUserDrivingLicense: PASSED");
        }
    }

    @Test
    void testShouldValidateWhenCustomerActuallyStolen() {
        try {
            HashMap<String, String> licenseSuspendedLostStolen = dmvValidatorService.isLicenseSuspendedLostStolen(customer);
            assertThat(licenseSuspendedLostStolen.get(DmvValidatorServiceImpl.DMVType.STATUS_TYPE.value)).isNotEqualTo(DmvValidatorServiceImpl.DMVType.LOST.value);
            LOGGER.info("testShouldValidateWhenCustomerActuallyStolen: PASSED");
        } catch (IOException | ResourceNotFoundException e) {
            fail("testShouldValidateWhenCustomerActuallyStolen: PASSED");
        }
    }

    @Test
    void testShouldDenoteThatCustomerLicenseIsValid() {
        try {
            HashMap<String, String> licenseSuspendedLostStolen = dmvValidatorService.isLicenseSuspendedLostStolen(validLicenseCustomer);
            assertThat(licenseSuspendedLostStolen.get(DmvValidatorServiceImpl.DMVType.STATUS_TYPE.value)).isEqualTo(DmvValidatorServiceImpl.DMVType.VALID.value);
            LOGGER.info("testShouldDenoteThatCustomerLicenseIsValid: PASSED");
        } catch (IOException | ResourceNotFoundException e) {
            fail("testShouldDenoteThatCustomerLicenseIsValid: PASSED");
        }
    }
}