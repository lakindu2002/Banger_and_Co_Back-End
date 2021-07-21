package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.*;
import com.lakindu.bangerandcobackend.entity.*;
import com.lakindu.bangerandcobackend.repository.RentalRepository;
import com.lakindu.bangerandcobackend.serviceinterface.AdditionalEquipmentService;
import com.lakindu.bangerandcobackend.serviceinterface.RentalService;
import com.lakindu.bangerandcobackend.serviceinterface.UserService;
import com.lakindu.bangerandcobackend.serviceinterface.VehicleService;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotCreatedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.mailsender.MailSender;
import com.lakindu.bangerandcobackend.util.mailsender.MailSenderHelper;
import com.lakindu.bangerandcobackend.util.mailsender.MailTemplateType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final AdditionalEquipmentService additionalEquipmentService;
    private final MailSender mailSender;

    private final int PRICE_PER_DAY_DIVISOR = 24; //price_per_day/24 = price per hour
    private final int ITEMS_PER_PAGE = 3;

    private final Logger LOGGER = Logger.getLogger(RentalServiceImpl.class.getName());

    public RentalServiceImpl(
            @Qualifier("rentalRepository") RentalRepository rentalRepository,
            @Qualifier("vehicleServiceImpl") VehicleService vehicleService,
            @Qualifier("userServiceImpl") UserService userService,
            @Qualifier("additionalEquipmentServiceImpl") AdditionalEquipmentService additionalEquipmentService,
            @Qualifier("mailSender") MailSender mailSender
    ) {
        this.rentalRepository = rentalRepository;
        this.vehicleService = vehicleService;
        this.userService = userService;
        this.additionalEquipmentService = additionalEquipmentService;
        this.mailSender = mailSender;
    }

    static class RentalEquipmentCalculatorSupporter {
        private List<RentalCustomization> rentalCustomizationList;
        private double totalCostForAdditionalEquipment;

        public List<RentalCustomization> getRentalCustomizationList() {
            return rentalCustomizationList;
        }

        public void setRentalCustomizationList(List<RentalCustomization> rentalCustomizationList) {
            this.rentalCustomizationList = rentalCustomizationList;
        }

        public double getTotalCostForAdditionalEquipment() {
            return totalCostForAdditionalEquipment;
        }

        public void setTotalCostForAdditionalEquipment(double totalCostForAdditionalEquipment) {
            this.totalCostForAdditionalEquipment = totalCostForAdditionalEquipment;
        }
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

        User theCustomer = userService._getUserWithoutDecompression(theRental.getCustomerUsername());

        if (theCustomer.isBlackListed()) {
            //customer cannot rent since they are blacklisted
            throw new ResourceNotCreatedException("Your account has been blacklisted as you have a rental that you have not picked up. Therefore, until the administrator whitelists you, you cannot make any rentals at Banger and Co.");
        }

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
                RentalEquipmentCalculatorSupporter equipmentsAdded = getEquipmentsAndTotalPriceForEquipments(theRental.getEquipmentsAddedToRental(), rentalPeriodInHours, theRentalToBeMade);
                theRentalToBeMade.setTotalCost(equipmentsAdded.getTotalCostForAdditionalEquipment() + costForVehicle);
                theRentalToBeMade.setRentalCustomizationList(equipmentsAdded.getRentalCustomizationList());
            }
            theRentalToBeMade.setPickupDate(theDateTime.getPickupDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            theRentalToBeMade.setReturnDate(theDateTime.getReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            theRentalToBeMade.setReturnTime(theDateTime.getReturnTime());
            theRentalToBeMade.setPickupTime(theDateTime.getPickupTime());
            theRentalToBeMade.setTotalCost(theRental.getTotalCostForRental());
            theRentalToBeMade.setVehicleOnRental(theVehicle);
            theRentalToBeMade.setTheCustomerRenting(theCustomer);

            Rental madeRental = rentalRepository.save(theRentalToBeMade); //create the rental.
            //email the client.
            try {
                mailSender.sendRentalMail(new MailSenderHelper(theCustomer, "Rental Made Successfully", MailTemplateType.RENTAL_MADE), madeRental);
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.warning("EMAIL NOT SENT DURING RENTAL: " + ex.getMessage());
            }
        } else {
            throw new ResourceNotCreatedException("The rental could not be created because the vehicle was not available for the specified pickup and return duration");
        }

    }

    /**
     * Method will get a list of all pending rentals from the database.
     *
     * @return - The list of vehicles that are pending.
     */
    @Override
    public HashMap<String, Object> getAllPendingRentals(int pageNumber) throws DataFormatException, IOException, ResourceNotFoundException {
        HashMap<String, Object> returnList = new HashMap<String, Object>();

        Pageable thePage = PageRequest.of(pageNumber, ITEMS_PER_PAGE);
        List<Rental> allPendingRentals = rentalRepository.getAllByIsApprovedEquals(
                false, thePage
        );

        List<RentalShowDTO> theRentalList = new ArrayList<>();

        for (Rental eachRental : allPendingRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theRentalList.add(rentalShowDTO);
        }

        returnList.put("nextPageNumber", pageNumber + 1);
        returnList.put("thePendingRentals", theRentalList);

        return returnList;
    }

    private RentalShowDTO convertToDTO(Rental eachRental) throws DataFormatException, IOException, ResourceNotFoundException {
        RentalShowDTO theDTO = new RentalShowDTO();

        theDTO.setRentalId(eachRental.getRentalId());
        theDTO.setPickupDate(eachRental.getPickupDate());
        theDTO.setReturnDate(eachRental.getReturnDate());
        theDTO.setPickupTime(eachRental.getPickupTime());
        theDTO.setReturnTime(eachRental.getReturnTime());
        theDTO.setTotalCostForRental(eachRental.getTotalCost());
        theDTO.setReturned(eachRental.getReturned());
        theDTO.setApproved(eachRental.getApproved());
        theDTO.setCollected(eachRental.getCollected());
        theDTO.setLateReturnRequested(eachRental.getLateReturnRequested());
        theDTO.setVehicleToBeRented(vehicleService.getVehicleById(eachRental.getVehicleOnRental().getVehicleId()));
        theDTO.setEquipmentsAddedToRental(additionalEquipmentService.getEquipmentForRental(eachRental.getRentalCustomizationList()));

        return theDTO;
    }

    /**
     * Method will blacklist customers if they have a rental that they have not picked up.
     * All blacklisted customers will be notified via an email informing about their account being blacklisted.
     * <br>
     * <b>Business Rule: </b> If the rental has been approved and has not been collected even after the day of return, the user will get blacklisted.
     */
    @Override
    public void blacklistCustomers() throws ResourceNotFoundException {
        List<User> blacklistedUsers = new ArrayList<>();
        List<Rental> allRentalsThatHavePassedReturnDate = rentalRepository.getAllRentalsThatHavePassedReturnDate(
                LocalDate.now(), true, false);

        for (Rental eachRental : allRentalsThatHavePassedReturnDate) {
            User theCustomerNotCollected = eachRental.getTheCustomerRenting();
            List<RentalCustomization> rentalCustomizationList = eachRental.getRentalCustomizationList();

            for (RentalCustomization eachCustomization : rentalCustomizationList) {
                additionalEquipmentService.addQuantityBackToItem(eachCustomization);
            }
            userService.blackListCustomer(theCustomerNotCollected, eachRental);
            blacklistedUsers.add(theCustomerNotCollected);
            rentalRepository.delete(eachRental);
        }

        //send an email to all the administrators in the system regarding the blacklisted customers
        List<User> adminList = userService._getAllAdministrators();
        try {
            mailSender.sendBulkRentalEmails(
                    adminList, "Blacklist Job Report", blacklistedUsers, MailTemplateType.ADMIN_BULK_BLACKLIST
            );
        } catch (Exception ex) {
            LOGGER.warning("EMAIL NOT SENT DURING BLACKLIST");
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
     * @param theRentalToBeMade
     * @return The object consisting of total cost for rental and equipments in the rental.
     * @throws ResourceNotFoundException   Thrown when the equipments cannot be found
     * @throws ResourceNotCreatedException Thrown when the equipment quantity exceeds three.
     */
    private RentalEquipmentCalculatorSupporter getEquipmentsAndTotalPriceForEquipments(List<AdditionalEquipmentDTO> equipmentsAddedToRental, long rentalPeriodInHours, Rental theRentalToBeMade) throws ResourceNotFoundException, ResourceNotCreatedException {
        RentalEquipmentCalculatorSupporter supporter = new RentalEquipmentCalculatorSupporter();
        List<RentalCustomization> addedCustomization = new ArrayList<>();
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

                    RentalCustomization eachCustomization = new RentalCustomization();
                    eachCustomization.setTheRentalInformation(theRentalToBeMade);
                    eachCustomization.setEquipmentAddedToRental(item);
                    eachCustomization.setQuantityAddedForEquipmentInRental(eachEquipment.getQuantitySelectedForRental());

                    addedCustomization.add(eachCustomization);

                    double costForEachEquipment = item.getPricePerDay() / PRICE_PER_DAY_DIVISOR;
                    totalPriceForEquipments += costForEachEquipment * rentalPeriodInHours;
                }
            }
        }
        supporter.setRentalCustomizationList(addedCustomization);
        supporter.setTotalCostForAdditionalEquipment(totalPriceForEquipments);
        return supporter;
    }
}
