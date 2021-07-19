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
import java.time.*;
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

    class RentalEquipmentCalculatorSupporter {
        public List<AdditionalEquipment> getEquipmentsInRental() {
            return equipmentsInRental;
        }

        public void setEquipmentsInRental(List<AdditionalEquipment> equipmentsInRental) {
            this.equipmentsInRental = equipmentsInRental;
        }

        public double getTotalCostForAdditionalEquipment() {
            return totalCostForAdditionalEquipment;
        }

        public void setTotalCostForAdditionalEquipment(double totalCostForAdditionalEquipment) {
            this.totalCostForAdditionalEquipment = totalCostForAdditionalEquipment;
        }

        private List<AdditionalEquipment> equipmentsInRental;
        private double totalCostForAdditionalEquipment;
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

        LocalDateTime pickupDateTime = LocalDateTime.of(theDateTime.getPickupDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), theDateTime.getPickupTime());
        LocalDateTime returnDateTime = LocalDateTime.of(theDateTime.getReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), theDateTime.getReturnTime());

        //check if duration is maximum of 2 weeks and start and end time is between 8 and 18:00
        //if same day rental, check if minimum of 5 hours is present.
        validateRentalFilters(theDateTime);
        //date and time is valid.

        //check if vehicle is free on given duration.
        Vehicle theVehicle = vehicleService._getVehicleInformation(theRental.getVehicleToBeRented());
        long rentalPeriodInHours = pickupDateTime.until(returnDateTime, ChronoUnit.HOURS);

        boolean isVehicleAvailable = vehicleService.isVehicleAvailableOnGivenDates(theVehicle, pickupDateTime, returnDateTime);
        if (isVehicleAvailable) {
            double costForVehicle = calculateCostForVehicle(rentalPeriodInHours, theVehicle);
            //vehicle can be rented.
            //if rental has additional equipment, get the total and reduce quantity from database.
            if (theRental.getEquipmentsAddedToRental().isEmpty()) {
                //no additional equipment, directly create rental.
                theRentalToBeMade.setTotalCost(costForVehicle);
            } else {
                //have additional equipment, reduce the quantity of the equipment and calculate cost for it.
                RentalEquipmentCalculatorSupporter equipmentsAdded = getEquipmentsAndTotalPriceForEquipments(theRental.getEquipmentsAddedToRental(), rentalPeriodInHours);
                theRentalToBeMade.setTotalCost(equipmentsAdded.getTotalCostForAdditionalEquipment() + costForVehicle);
                theRentalToBeMade.setEquipmentsAddedToRental(equipmentsAdded.getEquipmentsInRental());
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

    /**
     * Calculates the total price of the vehicle for the duration of the rental.
     *
     * <p>Price calculated with price per hour.</p>
     * <p>Per Per Day/24 = Price Per Hour</p>
     * <p>Duration in hours * Price per hour = cost of vehicle for rental.</p>
     *
     * @param rentalPeriodInHours The period of the rental in hours.
     * @param theVehicle          The vehicle being rented
     * @return The total cost for the vehicle for the period of rental.
     */
    private double calculateCostForVehicle(long rentalPeriodInHours, Vehicle theVehicle) {
        //get price per hour for vehicle
        double pricePerHourForVehicle = theVehicle.getTheVehicleType().getPricePerDay() / PRICE_PER_DAY_DIVISOR;
        //price per hour * price per hour for vehicle = price for vehicle on rental.
        return pricePerHourForVehicle * rentalPeriodInHours;
    }

    /**
     * Retrieves a list of the equipments added to the rental after their quantities have been deducted for the quantities added to the rental.
     *
     * @param equipmentsAddedToRental The equipments customer want in the rental
     * @param rentalPeriodInHours     The duration of the rental in hours.
     * @return The object consisting of total cost for rental and equipments in the rental.
     * @throws ResourceNotFoundException   Thrown when the equipments cannot be found
     * @throws ResourceNotCreatedException Thrown when the equipment quantity exceeds three.
     */
    private RentalEquipmentCalculatorSupporter getEquipmentsAndTotalPriceForEquipments(List<AdditionalEquipmentDTO> equipmentsAddedToRental, long rentalPeriodInHours) throws ResourceNotFoundException, ResourceNotCreatedException {
        RentalEquipmentCalculatorSupporter supporter = new RentalEquipmentCalculatorSupporter();
        List<AdditionalEquipment> addedEquipment = new ArrayList<>();
        double totalPriceForEquipments = 0;

        for (AdditionalEquipmentDTO eachEquipment : equipmentsAddedToRental) {
            //cannot select more than 3 for each equipment
            if (eachEquipment.getQuantitySelectedForRental() > 3) {
                throw new ResourceNotCreatedException("The equipment " + eachEquipment.getEquipmentName() + " exceed more than 3, therefore rental cannot be made. Maximum addon quantity should be 3.");
            } else {
                if (eachEquipment.getQuantitySelectedForRental() != 0) {
                    AdditionalEquipment item = additionalEquipmentService._getAdditionalEquipmentById(eachEquipment.getEquipmentId());
                    //calculate total price for rental and reduce equipment quantity as rental is made.
                    item.setEquipmentQuantity(item.getEquipmentQuantity() - eachEquipment.getQuantitySelectedForRental());
                    addedEquipment.add(item);

                    double costForEachEquipment = item.getPricePerDay() / PRICE_PER_DAY_DIVISOR;
                    totalPriceForEquipments += costForEachEquipment * rentalPeriodInHours;
                }
            }
        }
        supporter.setEquipmentsInRental(addedEquipment);
        supporter.setTotalCostForAdditionalEquipment(totalPriceForEquipments);
        return supporter;
    }
}
