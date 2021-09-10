package com.lakindu.bangerandcobackend.utils;

import com.lakindu.bangerandcobackend.entity.*;
import com.lakindu.bangerandcobackend.repository.*;
import com.lakindu.bangerandcobackend.util.FileHandler.ImageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
        customer.setEmailAddress("lakinduhewa@gmail.com");
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

        User customer2 = new User();
        customer2.setFirstName("Johnny");
        customer2.setLastName("Doe");
        customer2.setEmailAddress("john1@gmail.com");
        customer2.setContactNumber("0777778542");
        customer2.setUsername("johnydoe");
        customer2.setUserRole(roleList.get(1));
        customer2.setProfilePicture(handler.compressImage(new byte[]{}));
        customer2.setDrivingLicense(handler.compressImage(new byte[]{}));
        customer2.setOtherIdentity(handler.compressImage(new byte[]{}));
        customer2.setDrivingLicenseNumber("C1231231");
        customer2.setDateOfBirth(new Date(System.currentTimeMillis()));
        customer2.setBlackListed(false);
        customer2.setUserPassword(passwordEncoder.encode("test123"));

        User customer3 = new User();
        customer3.setFirstName("John");
        customer3.setLastName("Doe");
        customer3.setEmailAddress("cb007787@students.apiit.lk");
        customer3.setContactNumber("0777778542");
        customer3.setUsername("cb007787");
        customer3.setUserRole(roleList.get(1));
        customer3.setProfilePicture(handler.compressImage(new byte[]{}));
        customer3.setDrivingLicense(handler.compressImage(new byte[]{}));
        customer3.setOtherIdentity(handler.compressImage(new byte[]{}));
        customer3.setDrivingLicenseNumber("X1311312");
        customer3.setDateOfBirth(new Date(System.currentTimeMillis()));
        customer3.setBlackListed(false);
        customer3.setUserPassword(passwordEncoder.encode("test123"));


        List<User> users = Arrays.asList(customer, admin, customer2, customer3);
        return userRepository.saveAll(users);
    }

    public void deleteRolesAndUsers() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    public List<Inquiry> createInquiries() {
        Inquiry one = new Inquiry();
        one.calculateLodgedTime();
        one.setReplied(false);
        one.setFirstName("Lakindu");
        one.setLastName("Hewawasam");
        one.setInquirySubject("Test Subject");
        one.setMessage("Test Message");
        one.setContactNumber("0777790875");
        one.setEmailAddress("lakinduhewa@gmail.com");

        Inquiry two = new Inquiry();
        two.calculateLodgedTime();
        two.setReplied(false);
        two.setFirstName("Lakindu");
        two.setLastName("Hewawasam");
        two.setInquirySubject("Test Subject");
        two.setMessage("Test Message");
        two.setContactNumber("0777790875");
        two.setEmailAddress("lakinduhewa@gmail.com");

        Inquiry three = new Inquiry();
        three.calculateLodgedTime();
        three.setReplied(false);
        three.setFirstName("Lakindu");
        three.setLastName("Hewawasam");
        three.setInquirySubject("Test Subject");
        three.setMessage("Test Message");
        three.setContactNumber("0777790875");
        three.setEmailAddress("lakinduhewa@gmail.com");

        List<Inquiry> inquiries = Arrays.asList(one, two, three);
        return inquiryRepository.saveAll(inquiries);
    }

    public void deleteInquiries() {
        inquiryRepository.deleteAll();
    }

    public void removeVehicleTypes() {
        vehicleTypeRepository.deleteAll();
    }

    public List<VehicleType> createVehicleTypes() {
        VehicleType one = new VehicleType();
        one.setTypeName("TOWN CARS");
        one.setSize("SMALL");
        one.setVehicleList(new ArrayList<>());
        one.setPricePerDay(350);

        VehicleType two = new VehicleType();
        two.setTypeName("MINI VAN");
        two.setSize("LARGE");
        two.setVehicleList(new ArrayList<>());
        two.setPricePerDay(350);

        VehicleType three = new VehicleType();
        three.setTypeName("TOWN CARS");
        three.setSize("MEDIUM");
        three.setVehicleList(new ArrayList<>());
        three.setPricePerDay(350);

        List<VehicleType> vehicleTypes = Arrays.asList(one, two, three);
        return vehicleTypeRepository.saveAll(vehicleTypes);
    }

    public List<Vehicle> createVehicles(List<VehicleType> vehicleTypes) throws IOException {
        ImageHandler handler = new ImageHandler();


        Vehicle one = new Vehicle();
        one.setVehicleImage(handler.compressImage(new byte[]{}));
        one.setTheVehicleType(vehicleTypes.get(0));
        one.setVehicleName("Mercedes Benz");
        one.setRentalsForTheVehicle(new ArrayList<>());
        one.setFuelType("Petrol");
        one.setLicensePlate("KF-7895");
        one.setSeatingCapacity(8);
        one.setTransmission("Automatic");

        Vehicle two = new Vehicle();
        two.setVehicleImage(handler.compressImage(new byte[]{}));
        two.setTheVehicleType(vehicleTypes.get(0));
        two.setVehicleName("Mercedes Benz");
        two.setRentalsForTheVehicle(new ArrayList<>());
        two.setFuelType("Petrol");
        two.setLicensePlate("CSA-9862");
        two.setSeatingCapacity(8);
        two.setTransmission("Automatic");

        Vehicle three = new Vehicle();
        three.setVehicleImage(handler.compressImage(new byte[]{}));
        three.setTheVehicleType(vehicleTypes.get(0));
        three.setVehicleName("Mercedes Benz");
        three.setRentalsForTheVehicle(new ArrayList<>());
        three.setFuelType("Petrol");
        three.setLicensePlate("CSA-9352");
        three.setSeatingCapacity(8);
        three.setTransmission("Automatic");

        List<Vehicle> vehicles = Arrays.asList(one, two, three);
        return vehicleRepository.saveAll(vehicles);
    }

    public void deleteVehicles() {
        vehicleRepository.deleteAll();
        vehicleTypeRepository.deleteAll();
    }

    public String constructAPIUrl(int port, String endpoint) {
        return String.format("http://localhost:%d/api/%s", port, endpoint);
    }
}
