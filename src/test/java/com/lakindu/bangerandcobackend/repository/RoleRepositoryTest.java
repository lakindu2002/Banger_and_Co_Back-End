package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;
    private Role createdRole;
    private Logger LOGGER = Logger.getLogger(RoleRepositoryTest.class.getName());

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleName("administrator");
        role.setUsersInEachRole(new ArrayList<>());

        createdRole = roleRepository.save(role);
    }

    @AfterEach
    void tearDown() {
        createdRole = null;
        roleRepository.deleteAll();
    }

    @Test
    void testShouldGetAdministratorRole() {
        String roleNameToGet = "administrator";
        Role roleByRoleName = roleRepository.findRoleByRoleName(roleNameToGet);
        assertThat(roleByRoleName.getRoleName()).isEqualToIgnoringCase(roleNameToGet);
        LOGGER.info("testShouldGetAdministratorRole: PASSED");
    }

    @Test
    void testShouldNotGetRoleDueToCaseInSensitive() {
        String roleNameToGet = "customeR";
        Role roleByRoleName = roleRepository.findRoleByRoleName(roleNameToGet);
        assertThat(roleByRoleName).isNull();
        LOGGER.info("testShouldNotGetRoleDueToCaseInSensitive: PASSED");
    }

    @Test
    void testShouldNotGetRoleByRoleNameWhenRoleNameIsInvalid() {
        String roleNameToGet = "admins";
        Role roleByRoleName = roleRepository.findRoleByRoleName(roleNameToGet);
        assertThat(roleByRoleName).isNull();
        LOGGER.info("testShouldNotGetRoleByRoleNameWhenRoleNameIsInvalid: PASSED");
    }
}