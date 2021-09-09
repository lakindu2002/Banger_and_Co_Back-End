package com.lakindu.bangerandcobackend.utils;

import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
}
