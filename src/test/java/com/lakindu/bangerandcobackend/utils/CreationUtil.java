package com.lakindu.bangerandcobackend.utils;

import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.entity.Role;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.repository.*;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CreationUtil {
    @Autowired
    private AdditionalEquipmentRepository additionalEquipmentRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private RentalCustomizationRepository rentalCustomizationRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    @Qualifier("passwordEncoder")
    private PasswordEncoder passwordEncoder;

    public List<AdditionalEquipment> createAdditionalEquipments() {
        AdditionalEquipment one = new AdditionalEquipment();
        one.setEquipmentName("Sav Nav");
        one.setPricePerDay(500);
        one.setRentalsHavingThisCustomization(new ArrayList<>());
        one.setEquipmentQuantity(0);

        AdditionalEquipment two = new AdditionalEquipment();
        two.setEquipmentName("Wine Chiller");
        two.setPricePerDay(500);
        two.setRentalsHavingThisCustomization(new ArrayList<>());
        two.setEquipmentQuantity(50);

        AdditionalEquipment three = new AdditionalEquipment();
        three.setEquipmentName("Baby Seat");
        three.setPricePerDay(500);
        three.setRentalsHavingThisCustomization(new ArrayList<>());
        three.setEquipmentQuantity(50);

        List<AdditionalEquipment> additionalEquipments = Arrays.asList(one, two, three);
        return additionalEquipmentRepository.saveAll(additionalEquipments);
    }

    public void removeAllAdditionalEquipments() {
        additionalEquipmentRepository.deleteAll();
    }

    public List<Role> createRoles() {
        Role admin = new Role();
        admin.setRoleName("administrator");
        admin.setUsersInEachRole(new ArrayList<>());

        Role customer = new Role();
        customer.setRoleName("customer");
        customer.setUsersInEachRole(new ArrayList<>());

        List<Role> roles = Arrays.asList(admin, customer);
        return roleRepository.saveAll(roles);
    }

    public void deleteRoles() {
        roleRepository.deleteAll();
    }

    public List<User> createUsersAndRoles() throws IOException {
        ImageHandler handler = new ImageHandler();

        List<Role> roleList = createRoles();
        User customer = new User();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmailAddress("john@gmail.com");
        customer.setContactNumber("0777778542");
        customer.setUsername("johndoe");
        customer.setUserRole(roleList.get(1));
        customer.setProfilePicture(handler.compressImage(new byte[]{}));
        customer.setDrivingLicense(handler.compressImage(new byte[]{}));
        customer.setOtherIdentity(handler.compressImage(new byte[]{}));
        customer.setDrivingLicenseNumber("B1231231");
        customer.setDateOfBirth(new Date(System.currentTimeMillis()));
        customer.setBlackListed(false);
        customer.setUserPassword(passwordEncoder.encode("test123"));

        User admin = new User();
        admin.setFirstName("Jaden");
        admin.setLastName("Smith");
        admin.setEmailAddress("jaden@gmail.com");
        admin.setContactNumber("0777778542");
        admin.setUsername("jadensmith");
        admin.setUserRole(roleList.get(0));
        admin.setProfilePicture(handler.compressImage(new byte[]{}));
        admin.setDateOfBirth(new Date(System.currentTimeMillis()));
        admin.setBlackListed(false);
        admin.setUserPassword(passwordEncoder.encode("test123"));

        List<User> users = Arrays.asList(customer, admin);
        return userRepository.saveAll(users);
    }

    public void deleteRolesAndUsers() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}
