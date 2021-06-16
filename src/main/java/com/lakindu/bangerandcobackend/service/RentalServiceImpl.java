package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.repository.RentalRepository;
import com.lakindu.bangerandcobackend.serviceinterface.RentalService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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
        LocalDate pickupDate = theFilterDTO.getPickupDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate returnDate = theFilterDTO.getReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        //validations required on filter logic to ensure business rules are met

        //1. Pickup and return dates must fall between 8:00am to 6:00pm
        //2. Check if return date is before pickup date
        //2. Maximum Rental Duration is 14 days
        //3. If the rental day is one day, minimum duration is 5 hours

        //check if the pickup time and return time falls between 8 and 6
        LocalTime bangerMinStartTime = LocalTime.of(8, 0); //construct a LocalTime for minimum time banger allows
        LocalTime bangerMaxEndTime = LocalTime.of(18, 0); //construct a LocalTime for maximum time banger allows.

        if (theFilterDTO.getPickupTime().isBefore(bangerMinStartTime)) {
            //if pickup time is before 8
            throw new BadValuePassedException("The pickup time cannot be before 8:00 AM");
        }

        if (theFilterDTO.getPickupTime().isAfter(bangerMaxEndTime)) {
            //if pickup time is after 6pm
            throw new BadValuePassedException("The pickup time cannot be after 6:00 PM");
        }

        if (theFilterDTO.getReturnTime().isBefore(bangerMinStartTime)) {
            //if return time is before 8:00AM
            throw new BadValuePassedException("The return time cannot be before 8:00 AM");
        }

        if (theFilterDTO.getReturnTime().isAfter(bangerMaxEndTime)) {
            //if return time is after 6:00pm
            throw new BadValuePassedException("The return time cannot be after 6:00 PM");
        }

        if (returnDate.isBefore(pickupDate)) {
            //if return date is before pickup time
            throw new BadValuePassedException("Return date cannot be before Pickup Date");
        }

        //until method calculates amount of time from the pickupDate to returnDate
        if ((pickupDate.until(returnDate, ChronoUnit.DAYS) > 14)) {
            //if the return date is greater than 14 days.
            throw new BadValuePassedException("The maximum rental duration cannot exceed 14 days");
        }

        //if the rental day is one day, check if the minimum duration is 5 hours
        if (theFilterDTO.getPickupDate().equals(theFilterDTO.getReturnDate())) {
            //if rental duration is one day, check if minimum duration is 5 hours
            if (theFilterDTO.getPickupTime().until(theFilterDTO.getReturnTime(), ChronoUnit.HOURS) < 5) {
                //minimum duration is less than 5 hours
                throw new BadValuePassedException("The minimum rental duration must be 5 hours");
            }
        }
    }
}
