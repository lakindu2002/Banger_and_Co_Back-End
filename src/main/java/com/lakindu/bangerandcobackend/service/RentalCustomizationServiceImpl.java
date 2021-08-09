package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.repository.RentalCustomizationRepository;
import com.lakindu.bangerandcobackend.serviceinterface.RentalCustomizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class RentalCustomizationServiceImpl implements RentalCustomizationService {
    private final RentalCustomizationRepository rentalCustomizationRepository;

    @Autowired
    public RentalCustomizationServiceImpl(@Qualifier("rentalCustomizationRepository") RentalCustomizationRepository rentalCustomizationRepository) {
        this.rentalCustomizationRepository = rentalCustomizationRepository;
    }


    @Override
    @Transactional(rollbackOn = {Exception.class})
    public void deleteByRental(Rental rentalToBeCustomized) {
        //clear the additional equipment from the rental so that the update operation can be carried out from new.
        rentalCustomizationRepository.deleteRentalCustomizationsByTheRentalInformationEquals(rentalToBeCustomized);
    }
}
