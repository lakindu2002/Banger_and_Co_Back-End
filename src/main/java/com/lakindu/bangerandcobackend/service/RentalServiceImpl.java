package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.repository.RentalRepository;
import com.lakindu.bangerandcobackend.serviceinterface.RentalService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Date;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;

    public RentalServiceImpl(
            @Qualifier("rentalRepository") RentalRepository rentalRepository
    ) {
        this.rentalRepository = rentalRepository;
    }


    @Override
    public void validateRentalFilters(VehicleRentalFilterDTO theFilterDTO) throws BadValuePassedException {
        Date pickupDate = theFilterDTO.getPickupDate();
        Date returnDate = theFilterDTO.getReturnDate();
        LocalTime pickupTime = theFilterDTO.getPickupTime();
        LocalTime returnTime = theFilterDTO.getReturnTime();

        //validations required on filter logic to ensure business rules are met

        //1. Maximum Rental Duration is 14 days
        //2. If the rental day is one day, minimum duration is 5 days.
        //3. Pickup and return dates must fall between 8:00am to 6:00pm

    }
}
