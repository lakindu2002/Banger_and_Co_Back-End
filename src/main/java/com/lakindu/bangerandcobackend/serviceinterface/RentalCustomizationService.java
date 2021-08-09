package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.RentalCustomization;

import java.util.List;

public interface RentalCustomizationService {
    void deleteByRental(Rental rentalToBeCustomized);
}
