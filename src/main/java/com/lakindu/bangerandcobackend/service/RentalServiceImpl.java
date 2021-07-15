package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.AdditionalEquipmentDTO;
import com.lakindu.bangerandcobackend.dto.RentalCreateDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.repository.RentalRepository;
import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import com.lakindu.bangerandcobackend.serviceinterface.RentalService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotCreatedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final AdditionalEquipmentService additionalEquipmentService;
    private final int PRICE_PER_DAY_DIVISOR = 24; //price_per_day/24 = price per hour

    public RentalServiceImpl(
            @Qualifier("rentalRepository") RentalRepository rentalRepository,
            @Qualifier("vehicleServiceImpl") VehicleService vehicleService,
            @Qualifier("userServiceImpl") UserService userService,
            @Qualifier("additionalEquipmentServiceImpl") AdditionalEquipmentService additionalEquipmentService
    ) {
        this.rentalRepository = rentalRepository;
        this.vehicleService = vehicleService;
        this.userService = userService;
        this.additionalEquipmentService = additionalEquipmentService;
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

    @Override
    public void makeRental(RentalCreateDTO theRental) throws ParseException, BadValuePassedException, ResourceNotFoundException, ResourceNotCreatedException {
        Rental theRentalToBeMade = new Rental();

        VehicleRentalFilterDTO theDateTime = new VehicleRentalFilterDTO();
        theDateTime.setPickupDate(new SimpleDateFormat("yyyy-MM-dd").parse(theRental.getPickupDate()));
        theDateTime.setReturnDate(new SimpleDateFormat("yyyy-MM-dd").parse(theRental.getReturnDate()));
        theDateTime.setPickupTime(LocalTime.parse(theRental.getPickupTime()));
        theDateTime.setReturnTime(LocalTime.parse(theRental.getReturnTime()));

        //check if duration is maximum of 2 weeks and start and end time is between 8 and 18:00
        //if same day rental, check if minimum of 5 hours is present.
        validateRentalFilters(theDateTime);
        //date and time is valid.

        //check if vehicle is free on given duration.
        Vehicle theVehicle = vehicleService._getVehicleInformation(theRental.getVehicleToBeRented());
        LocalDateTime pickupDateTime = LocalDateTime.of(theDateTime.getPickupDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), theDateTime.getPickupTime());
        LocalDateTime returnDateTime = LocalDateTime.of(theDateTime.getReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), theDateTime.getReturnTime());


        boolean isVehicleAvailable = vehicleService.isVehicleAvailableOnGivenDates(theVehicle, pickupDateTime, returnDateTime);
        //calculate total cost for rental.

        if (isVehicleAvailable) {
            //vehicle can be rented.
            //if rental has additional equipment, get the total and reduce quantity from database.
            if (!theRental.getEquipmentsAddedToRental().isEmpty()) {
                //no additional equipment, directly create rental.
            }

            theRentalToBeMade.setPickupDate(theDateTime.getPickupDate());
            theRentalToBeMade.setReturnDate(theDateTime.getReturnDate());
            theRentalToBeMade.setReturnTime(theDateTime.getReturnTime());
            theRentalToBeMade.setPickupTime(theDateTime.getPickupTime());
            theRentalToBeMade.setTotalCost(theRental.getTotalCostForRental());
            theRentalToBeMade.setVehicleOnRental(theVehicle);
            theRentalToBeMade.setTheCustomerRenting(userService._getUserWithoutDecompression(theRental.getCustomerUsername()));

            Rental madeRental = rentalRepository.save(theRentalToBeMade); //create the rental.
            //email the client.
        } else {
            throw new ResourceNotCreatedException("The rental could not be created because the vehicle was not available for the specified pickup and return duration");
        }

    }

    private List<AdditionalEquipment> getAdditionalEquipmentForRental(ArrayList<AdditionalEquipmentDTO> equipmentsAddedToRental) throws ResourceNotFoundException {
        List<AdditionalEquipment> addedEquipment = new ArrayList<>();

        for (AdditionalEquipmentDTO eachEquipment : equipmentsAddedToRental) {
            AdditionalEquipment item = additionalEquipmentService._getAdditionalEquipmentById(eachEquipment.getEquipmentId());
            addedEquipment.add(item);
        }
        return addedEquipment;
    }
}
