package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.serviceinterface.RoleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.utils.CreationUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.logging.Logger;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoleServiceImplTest {

    @Autowired
    private CreationUtil creationUtil;
    @Autowired
    @Qualifier("roleServiceImpl")
    private RoleService roleService;
    private Logger LOGGER = Logger.getLogger(RoleServiceImplTest.class.getName());

    @BeforeAll
    void beforeAll() {
        creationUtil.createRoles();
    }

    @AfterAll
    void afterAll() {
        creationUtil.deleteRoles();
    }

    @Test
    void testShouldGetAdministratorRoleInformation() {
        try {
            String roleToGet = "administrator";
            Role administrator = roleService._getRoleInformation(roleToGet);
            assertThat(administrator.getRoleName()).isEqualTo(roleToGet);

            LOGGER.info("testShouldGetAdministratorRoleInformation: PASSED");
        } catch (Exception e) {
            fail("testShouldGetAdministratorRoleInformation: FAILED");
        }
    }

    @Test
    void testShouldGetCustomerRoleInformation() {
        try {
            String roleToGet = "customer";
            Role administrator = roleService._getRoleInformation(roleToGet);
            assertThat(administrator.getRoleName()).isEqualTo(roleToGet);

            LOGGER.info("testShouldGetCustomerRoleInformation: PASSED");
        } catch (Exception e) {
            fail("testShouldGetCustomerRoleInformation: FAILED");
        }
    }

    @Test
    void testShouldThrowExceptionWhenRoleNameIsInvalid() {
        try {
            String roleToGet = "testException";
            assertThrows(ResourceNotFoundException.class, () -> roleService._getRoleInformation(roleToGet));
            LOGGER.info("testShouldThrowExceptionWhenRoleNameIsInvalid: PASSED");
        } catch (Exception e) {
            fail("testShouldThrowExceptionWhenRoleNameIsInvalid: FAILED");
        }
    }
}