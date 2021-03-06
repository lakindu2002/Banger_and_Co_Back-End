package com.lakindu.bangerandcobackend.service;

import com.lakindu.bangerandcobackend.dto.*;
import com.lakindu.bangerandcobackend.entity.*;
import com.lakindu.bangerandcobackend.repository.RentalRepository;
import com.lakindu.bangerandcobackend.serviceinterface.*;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotCreatedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;
import com.lakindu.bangerandcobackend.util.mailsender.MailSender;
import com.lakindu.bangerandcobackend.util.mailsender.MailSenderHelper;
import com.lakindu.bangerandcobackend.util.mailsender.MailTemplateType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final AdditionalEquipmentService additionalEquipmentService;
    private final MailSender mailSender;
    private final RentalCustomizationService rentalCustomizationService;
    private final DmvValidatorService dmvValidatorService;

    //utils required for using the stored procedure.
    private final String INSURER_DB_CALL = "IS_USER_FRADULENT"; //stored procedure name
    private final JdbcTemplate jdbcTemplate; //allows working with relational db while taking care of exceptions & connections.
    private SimpleJdbcCall isUserFraudulentProcedureCall; //thread safe util used to communicate with a stored procedure.
    private final String JDBC_RESULT_CARRIER = "client_list"; //data available here after executing stored procedure.
    private final String INPUT_PARAM_STORED_PROCEDURE = "banger_customer_license_number";

    private final int PRICE_PER_DAY_DIVISOR = 24; //price_per_day/24 = price per hour
    private final int ITEMS_PER_PAGE = 10;

    private final Logger LOGGER = Logger.getLogger(RentalServiceImpl.class.getName());

    public RentalServiceImpl(
            @Qualifier("rentalRepository") RentalRepository rentalRepository,
            @Qualifier("vehicleServiceImpl") VehicleService vehicleService,
            @Qualifier("userServiceImpl") UserService userService,
            @Qualifier("additionalEquipmentServiceImpl") AdditionalEquipmentService additionalEquipmentService,
            @Qualifier("mailSender") MailSender mailSender,
            @Qualifier("rentalCustomizationServiceImpl") RentalCustomizationService rentalCustomizationService,
            @Qualifier("dmvValidatorServiceImpl") DmvValidatorService dmvValidatorService,
            @Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate
    ) {
        this.rentalRepository = rentalRepository;
        this.vehicleService = vehicleService;
        this.userService = userService;
        this.additionalEquipmentService = additionalEquipmentService;
        this.mailSender = mailSender;
        this.rentalCustomizationService = rentalCustomizationService;
        this.dmvValidatorService = dmvValidatorService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        //create a stored procedure call using the procedure name given below.
        //the stored procedure will map all results into a key named = "client_list" and will map each row as an instance of FraudClient
        isUserFraudulentProcedureCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(INSURER_DB_CALL)
                .returningResultSet(JDBC_RESULT_CARRIER, BeanPropertyRowMapper.newInstance(FraudClient.class));
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
        //2. Check if return date is after pickup date
        //2. Maximum Rental Duration is 14 days
        //3. If the rental day is one day, minimum duration is 5 hours

        //check if the pickup time and return time falls between 8 and 6
        LocalTime bangerMinStartTime = LocalTime.of(8, 0); //construct a LocalTime for minimum time banger allows
        LocalTime bangerMaxEndTime = LocalTime.of(18, 0); //construct a LocalTime for maximum time banger allows.

        LocalDateTime pickupDateTime = LocalDateTime.of(pickupDate, theFilterDTO.getPickupTime());
        if (pickupDateTime.isBefore(LocalDateTime.now())) {
            //user has selected a date before current date time, therefore cannot allow
            throw new BadValuePassedException("The pickup time must not be a past time");
        }

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
            //if return date is before pickup date
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
    public void makeRental(RentalCreateDTO theRental) throws ParseException, BadValuePassedException, ResourceNotFoundException, ResourceNotCreatedException, IOException {
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
            throw new ResourceNotCreatedException("Your account is blacklisted. Therefore, until the administrator whitelists you, you cannot make any rentals at Banger and Co.");
        }

        //validate the customer driving license to ensure the license is not lost, stolen, suspended.
        HashMap<String, String> validationDetails = dmvValidatorService.isLicenseSuspendedLostStolen(theCustomer);
        String statusOfLicense = validationDetails.get(DmvValidatorServiceImpl.DMVType.STATUS_TYPE.value);
        Date dateOfOffense = new Date();
        //validation for license
        //if license is suspended, lost, stolen do not go let rental to be created.
        if (statusOfLicense.equalsIgnoreCase(DmvValidatorServiceImpl.DMVType.SUSPENDED.value)) {
            LOGGER.warning("RENTAL NOT MADE: LICENSE SUSPENDED");
            //send email to DMV regarding the license status.
            mailSender.sendDmvEmail(theCustomer, statusOfLicense, dateOfOffense);
            //blacklist customer to prevent them from making any more rentals
            userService._blackListCustomer(theCustomer);
            throw new ResourceNotCreatedException("Your rental creation request was rejected because your driving license has been suspended by the DMV. Your account is now blacklisted");
        } else if (statusOfLicense.equalsIgnoreCase(DmvValidatorServiceImpl.DMVType.LOST.value)) {
            LOGGER.warning("RENTAL NOT MADE: LICENSE REPORTED LOST");
            //send email to DMV regarding the license status.
            mailSender.sendDmvEmail(theCustomer, statusOfLicense, dateOfOffense);
            //blacklist customer to prevent them from making any more rentals
            userService._blackListCustomer(theCustomer);
            throw new ResourceNotCreatedException("Your rental creation request was rejected because your driving license has been reported lost by the DMV. Your account is now blacklisted");
        } else if (statusOfLicense.equalsIgnoreCase(DmvValidatorServiceImpl.DMVType.STOLEN.value)) {
            LOGGER.warning("RENTAL NOT MADE: LICENSE REPORTED STOLEN");
            //send email to DMV regarding the license status.
            mailSender.sendDmvEmail(theCustomer, statusOfLicense, dateOfOffense);
            //blacklist customer to prevent them from making any more rentals
            userService._blackListCustomer(theCustomer);
            throw new ResourceNotCreatedException("Your rental creation request was rejected because your driving license has been reported as stolen by the DMV. Your account is now blacklisted");
        }

        //check if customer has fraudulent claims from the insurer db
        if (this.checkForFraudulentClaims(theCustomer)) {
            //have fraudulent claims,reject and send error
            String rejectReason = "This driving license number is associated to fraudulent claims as denoted by the Association of Sri Lankan Insurers, therefore, this rental cannot be created";
            throw new ResourceNotCreatedException(rejectReason);
        }

        boolean isVehicleAvailable = vehicleService.isVehicleAvailableOnGivenDates(theVehicle, pickupDateTime, returnDateTime);
        //exceptions will be thrown during validation for customer having pending,on-going,approved rentals
        isCustomerHavingPendingOnGoingApprovedRentalsForPeriod(theCustomer, pickupDateTime, returnDateTime);

        if (isVehicleAvailable) {
            double costForVehicle = calculateCostForVehicle(rentalPeriodInHours, theVehicle);
            //vehicle can be rented and customer does not have pending, on-going, approved rentals for given period.
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
            //email the client indicating rental was made.
            //email the admins indicating a rental was made
            try {
                mailSender.sendRentalMail(new MailSenderHelper(theCustomer, "Rental Made Successfully", MailTemplateType.RENTAL_MADE), madeRental);
                mailSender.notifyAllAdminsAboutNewRental(userService._getAllAdminEmails(), "A New Rental Was Made", madeRental, MailTemplateType.ADMIN_BULK_RENTAL_MADE);
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.warning("EMAIL NOT SENT DURING RENTAL: " + ex.getMessage());
            }

        } else {
            throw new ResourceNotCreatedException("The rental could not be created because the vehicle was not available for the specified pickup and return duration");
        }

    }

    /**
     * Method will check if the customer has any on-going, pending, approved rentals for the given time period.
     *
     * @param theCustomer    The customer that is renting
     * @param pickupDateTime The start time period
     * @param returnDateTime The end time period
     */
    @Override
    public void isCustomerHavingPendingOnGoingApprovedRentalsForPeriod(User theCustomer, LocalDateTime pickupDateTime, LocalDateTime returnDateTime) throws ResourceNotCreatedException {
        //get rentals for the customer
        List<Rental> allByTheCustomerRentingEquals = rentalRepository.getAllByTheCustomerRentingEquals(theCustomer);

        for (Rental eachRental : allByTheCustomerRentingEquals) {
            //retrieve LocalDateTime of the Rental Pickup - Date, Time and Return - Date, Time.
            LocalDateTime eachRentalPickupDateTime = LocalDateTime.of(eachRental.getPickupDate(), eachRental.getPickupTime());
            LocalDateTime eachRentalReturnDateTime = LocalDateTime.of(eachRental.getReturnDate(), eachRental.getReturnTime());

            //if filtering Pickup DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
            //OR
            //if filtering Return DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
            if (
                    ((pickupDateTime.isAfter(eachRentalPickupDateTime) || pickupDateTime.equals(eachRentalPickupDateTime)) && (pickupDateTime.isBefore(eachRentalReturnDateTime) || pickupDateTime.equals(eachRentalReturnDateTime)))
                            ||
                            ((returnDateTime.isAfter(eachRentalPickupDateTime) || returnDateTime.equals(eachRentalPickupDateTime)) && (returnDateTime.isBefore(eachRentalReturnDateTime) || returnDateTime.equals(eachRentalPickupDateTime)))

            ) {
                //The filtering Pickup DATE_TIME or Return DATE_TIME is between a rental.
                //customer cannot rent if he has ongoing, pending, approved rentals
                if (eachRental.getApproved() == null) {
                    //pending rentals are present
                    throw new ResourceNotCreatedException("You have pending rentals during this period, therefore this rental cannot be made");
                } else if (eachRental.getApproved() != null && eachRental.getApproved() && (eachRental.getCollected() != null && !eachRental.getCollected())) {
                    //approved, can be collected rentals are present
                    throw new ResourceNotCreatedException("You have rentals that can be collected during this period, therefore this rental cannot be made");
                } else if ((eachRental.getCollected() != null && eachRental.getCollected()) && eachRental.getReturned() != null && !eachRental.getReturned()) {
                    //collected rentals, ongoing rentals are present, not yet returned
                    throw new ResourceNotCreatedException("You have rentals that can be on-going during this period, therefore this rental cannot be made");
                }
            } else {
                //The filtering Pickup DATE_TIME or Return DATE_TIME is not between a rental.
                //BUT
                //there may be rentals present between passed PICKUP Date_Time AND RETURN Date_Time
                if (eachRentalPickupDateTime.isAfter(pickupDateTime) && eachRentalReturnDateTime.isBefore(returnDateTime)) {
                    //the rental in database is between the passed Pickup-Date_Time and Return Date_Time
                    //The filtering Pickup DATE_TIME or Return DATE_TIME is between a rental.
                    if (eachRental.getApproved() == null) {
                        //pending rentals are present
                        throw new ResourceNotCreatedException("You have pending rentals during this period, therefore this rental cannot be made");
                    } else if (eachRental.getApproved() != null && eachRental.getApproved() && (eachRental.getCollected() != null && !eachRental.getCollected())) {
                        //approved, can be collected rentals are present
                        throw new ResourceNotCreatedException("You have rentals that can be collected during this period, therefore this rental cannot be made");
                    } else if ((eachRental.getCollected() != null && eachRental.getCollected()) && eachRental.getReturned() != null && !eachRental.getReturned()) {
                        //collected rentals, ongoing rentals are present, not yet returned
                        throw new ResourceNotCreatedException("You have rentals that can be on-going during this period, therefore this rental cannot be made");
                    }
                }
            }
        }
    }

    /**
     * Method will be used to complete the rental only if it collected by the customer.
     *
     * <br>
     * <p>
     * After returning the rental, add the equipment stock back to the original quantity
     *
     * @param rentalId The rental to complete
     */
    @Override
    public void completeRental(Integer rentalId) throws ResourceNotFoundException, ResourceNotUpdatedException {
        Rental theRentalToBeReturned = rentalRepository.findById(rentalId).orElseThrow(() -> new ResourceNotFoundException("The rental you are trying to return cannot be found"));
        //check if the rental to be returned has been collected,
        //if the rental has been collected, check if it already returned, if so do not return again.

        boolean isRentalCollected = theRentalToBeReturned.getCollected() != null && theRentalToBeReturned.getCollected();
        boolean isRentalReturned = theRentalToBeReturned.getReturned() != null && theRentalToBeReturned.getReturned();

        if (isRentalCollected && isRentalReturned) {
            throw new ResourceNotUpdatedException("The rental has already been returned.");
        }

        if (!isRentalCollected) {
            //rental has not yet been collected
            throw new ResourceNotUpdatedException("The rental has not yet been collected. Therefore, it cannot be returned");
        }
        //validation have passed, return the rental
        theRentalToBeReturned.setReturned(true);

        //update quantity back
        for (RentalCustomization eachAddOn : theRentalToBeReturned.getRentalCustomizationList()) {
            additionalEquipmentService.addQuantityBackToItem(eachAddOn);
        }

        Rental returnedRental = rentalRepository.save(theRentalToBeReturned);//update the rental

        //send an email to the customer indicating rental has been collected.
        try {
            mailSender.sendRentalMail(
                    new MailSenderHelper(returnedRental.getTheCustomerRenting(), "Rental Completed", MailTemplateType.RENTAL_RETURNED),
                    returnedRental
            );
        } catch (IOException | MessagingException e) {
            LOGGER.warning("ERROR SENDING RETURNED EMAIl");
        }

    }

    @Override
    public List<ChartReturn> getCompletedRentalsForPast12Months() throws Exception {
        //method will be executed by the administrator to get the completed rentals for the past 12 months for the chart.
        Calendar dateTime12MonthsAgo = Calendar.getInstance();

        dateTime12MonthsAgo.set(Calendar.HOUR, 0); //set to midnight
        dateTime12MonthsAgo.set(Calendar.MINUTE, 0); //set to exactly 0th minute
        dateTime12MonthsAgo.set(Calendar.SECOND, 0); //set to 0 seconds
        dateTime12MonthsAgo.add(Calendar.MONTH, -12); //reduce 12 months from the current dateTime

        LocalDate startDate = LocalDate.from(dateTime12MonthsAgo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        //get all completed rentals between these 12 months ago and current date time
        List<Rental> completedPast12MonthsRentals = rentalRepository.getAllCompletedRentalsForPast12Months(startDate);
        List<ChartReturn> formattedDBData = compileInToMonthsAndCount(completedPast12MonthsRentals);

        List<ChartReturn> chartReturns = fillEmptyMonths(formattedDBData, dateTime12MonthsAgo);

        //sort to order the months from oldest (12 months ago) to current month
        chartReturns.sort((chart01, chart02) -> {
            DateFormat simpleDateFormat = new SimpleDateFormat("MMMM - yyyy"); //parse date in MONTH - year
            try {
                Date firstDate = simpleDateFormat.parse(chart01.getMonth()); //first month parsed in given format
                Date secondDate = simpleDateFormat.parse(chart02.getMonth()); //second month parsed in given format

                //(0) - same, (-1) - first < second, (1) first > second
                return firstDate.compareTo(secondDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });

        return chartReturns;
    }

    @Override
    public List<ChartReturn> getProfitsForLast12Months() throws Exception {
        List<ChartReturn> completedRentalsForPast12Months = getCompletedRentalsForPast12Months();
        for (ChartReturn eachMonth : completedRentalsForPast12Months) {
            double total = 0; //variable to keep track of total earnings per month
            List<RentalShowDTO> rentalsForMonth = eachMonth.getRentals();
            for (RentalShowDTO rentalShowDTO : rentalsForMonth) {
                total = total + rentalShowDTO.getTotalCostForRental();
            }
            eachMonth.setTotalForTheMonth(total); //calculate total cost
            eachMonth.setRentals(null); //no need to show rentals in return
        }
        return completedRentalsForPast12Months;
    }

    @Override
    public List<RentalShowDTO> getVehiclesToBeCollectedForMonth() throws Exception {
        Calendar endOfMonth = Calendar.getInstance();
        int maximumDate = endOfMonth.getActualMaximum(Calendar.DATE); //get the last date of the month
        endOfMonth.set(Calendar.DATE, maximumDate);

        Calendar beginningOfMonth = Calendar.getInstance();
        beginningOfMonth.set(Calendar.DATE, 1); //first date of the month


        List<Rental> vehiclesToBeCollected = rentalRepository.getAllByIsApprovedEqualsAndIsCollectedEqualsAndPickupDateGreaterThanEqualAndPickupDateLessThanEqual(
                true, false,
                beginningOfMonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                endOfMonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        );

        List<RentalShowDTO> showingRentals = new ArrayList<>();

        for (Rental eachRental : vehiclesToBeCollected) {
            //covert to dto list
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO vehicleShowDTO = rentalShowDTO.getVehicleToBeRented();
            if (vehicleShowDTO != null) {
                vehicleShowDTO.setVehicleImage(null);
            }

            UserDTO customerUsername = rentalShowDTO.getCustomerUsername();
            customerUsername.setProfilePicture(null);
            customerUsername.setOtherIdentity(null);
            customerUsername.setLicensePic(null);

            rentalShowDTO.setCustomerUsername(customerUsername);
            rentalShowDTO.setVehicleToBeRented(vehicleShowDTO);
            showingRentals.add(rentalShowDTO);
        }
        return showingRentals;
    }

    @Override
    public List<RentalShowDTO> getAllPendingRentalsForStatistics() throws Exception {
        List<Rental> allPendingRentals = rentalRepository.findAllByIsApprovedEquals(
                null
        );

        List<RentalShowDTO> theRentalList = new ArrayList<>();

        for (Rental eachRental : allPendingRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }

            UserDTO theCustomer = rentalShowDTO.getCustomerUsername();
            theCustomer.setLicensePic(null);
            theCustomer.setOtherIdentity(null);
            theCustomer.setProfilePicture(null);

            rentalShowDTO.setCustomerUsername(theCustomer);

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theRentalList.add(rentalShowDTO);
        }
        return theRentalList;
    }

    @Override
    public List<RentalShowDTO> getAllOnGoingRentalsForChart() throws Exception {
        List<Rental> allOngoingRentals = rentalRepository.findAllByIsApprovedEqualsAndIsCollectedEqualsAndIsReturnedEquals(
                true, true, false
        );

        List<RentalShowDTO> theRentalList = new ArrayList<>();

        for (Rental eachRental : allOngoingRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }

            UserDTO theCustomer = rentalShowDTO.getCustomerUsername();
            theCustomer.setLicensePic(null);
            theCustomer.setOtherIdentity(null);
            theCustomer.setProfilePicture(null);

            rentalShowDTO.setCustomerUsername(theCustomer);

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theRentalList.add(rentalShowDTO);
        }
        return theRentalList;
    }

    /**
     * Method will create a late return for the rental
     * <br>
     * The rental can be late returned only if the customer is a returning customer and if the rental has been collected and not returned.
     *
     * @param rentalId The rental for late request
     */
    @Override
    public void createLateReturnForRental(Integer rentalId, Authentication loggedInUser) throws ResourceNotFoundException, ResourceNotUpdatedException, BadValuePassedException {
        Rental rentalForLateRequest = rentalRepository.findById(rentalId).orElseThrow(() -> new ResourceNotFoundException("The rental you are trying to create the late return for does not exist at Banger and Co."));
        User customerRenting = rentalForLateRequest.getTheCustomerRenting();

        if (!loggedInUser.getName().equalsIgnoreCase(customerRenting.getUsername())) {
            throw new BadValuePassedException("The rental that is trying to returned late was not made by you");
        }

        //check if rental is on-going
        if ((rentalForLateRequest.getCollected() != null && rentalForLateRequest.getCollected()) && (rentalForLateRequest.getReturned() != null && !rentalForLateRequest.getReturned())) {
            //get set of completed rentals for the customer
            //null - no pagination
            List<Rental> customerRentals = rentalRepository.getAllByIsApprovedEqualsAndIsCollectedEqualsAndIsReturnedEqualsAndTheCustomerRentingEquals(
                    true, true, true, customerRenting, null
            );

            if (customerRentals.size() > 0) {
                //customer has completed rentals and is a returning customer

                if (rentalForLateRequest.getLateReturnRequested() == null) {
                    rentalForLateRequest.setLateReturnRequested(true); //rental will be late returned
                    Rental lateReturningRental = rentalRepository.save(rentalForLateRequest);

                    //send email to admin and customers regarding late return
                    try {
                        //email customer
                        mailSender.sendRentalMail(
                                new MailSenderHelper(customerRenting, "Late Return Notified", MailTemplateType.LATE_RETURN_CUSTOMER),
                                lateReturningRental
                        );

                        //email admins
                        mailSender.sendBulkRentalEmails(
                                userService._getAllAdminEmails(), "Late Return Notification", null, MailTemplateType.LATE_RETURN_ADMINS, lateReturningRental
                        );
                    } catch (Exception ex) {
                        LOGGER.warning("ERROR SENDING LATE RETURN EMAIL");
                    }
                } else if (rentalForLateRequest.getLateReturnRequested()) {
                    throw new ResourceNotUpdatedException("The rental has already been marked as late return");
                }

            } else {
                throw new ResourceNotUpdatedException("You cannot have a late return because you do not have any past completed rentals. Only returning customers are allowed to request for a late return");
            }
        } else {
            //rental is not on-going
            throw new ResourceNotUpdatedException("The rental in not an on-going rental. Therefore you cannot perform a late return for this rental");
        }
    }

    /**
     * Method will cancel a late return for a rental
     * <br>
     * In order to cancel a late return the rental must be on late return in the first place
     *
     * @param rentalId     The rental to cancel the late return for
     * @param loggedInUser The user logged in to the system == to the customer on rental being cancelled for late return
     */
    @Override
    public void cancelLateReturn(Integer rentalId, Authentication loggedInUser) throws ResourceNotFoundException, ResourceNotUpdatedException {
        Rental theRentalToCancelLateReturnFor = rentalRepository.findById(rentalId).orElseThrow(() -> new ResourceNotFoundException("The rental you are trying to cancel the late return for does not exist at Banger and Co."));
        if (theRentalToCancelLateReturnFor.getTheCustomerRenting().getUsername().equalsIgnoreCase(loggedInUser.getName())) {
            //customer is the same customer on rental
            if (theRentalToCancelLateReturnFor.getLateReturnRequested() != null && theRentalToCancelLateReturnFor.getLateReturnRequested()) {
                theRentalToCancelLateReturnFor.setLateReturnRequested(null); //when inserting, is set as NULL as no late return
                //so set it back to null again to cancel late return
                Rental cancelledLateRequestRental = rentalRepository.save(theRentalToCancelLateReturnFor);

                try {
                    //notify the customer with their cancel request being success
                    mailSender.sendRentalMail(
                            new MailSenderHelper(
                                    theRentalToCancelLateReturnFor.getTheCustomerRenting(),
                                    "Late Return Cancelled Successfully",
                                    MailTemplateType.LATE_RETURN_CANCELLED_CUSTOMER
                            ),
                            cancelledLateRequestRental
                    );

                    //notify the administrators regarding this cancel
                    mailSender.sendBulkRentalEmails(
                            userService._getAllAdminEmails(),
                            "Late Return Cancelled",
                            null,
                            MailTemplateType.LATE_RETURN_CANCELLED_ADMIN_BULK,
                            cancelledLateRequestRental
                    );
                } catch (IOException | MessagingException e) {
                    LOGGER.warning("ERROR SENDING EMAIL DURING LATE RETURN CANCEL");
                }

            } else {
                //rental not on late return
                throw new ResourceNotUpdatedException("This rental is not on late return, therefore, you cannot cancel the late return");
            }
        } else {
            throw new ResourceNotUpdatedException("The rental you are trying to cancel the late return for does not belong to you");
        }

    }

    /**
     * Method will get customer on-going rentals without any pagination.
     *
     * @param name The customer username
     */
    @Override
    public List<RentalShowDTO> getCustomerOnGoingRentals(String name) throws Exception {
        List<Rental> on_goingRentals = rentalRepository.getAllByIsApprovedEqualsAndIsCollectedEqualsAndIsReturnedEqualsAndTheCustomerRentingEquals(
                true, true, false, userService._getUserWithoutDecompression(name), null
        );

        List<RentalShowDTO> rentalShowDTOS = new ArrayList<>();

        for (Rental eachRental : on_goingRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO vehicleToBeRented = rentalShowDTO.getVehicleToBeRented();
            if (vehicleToBeRented != null) {
                vehicleToBeRented.setVehicleImage(null); //remove vehicle image
            }

            rentalShowDTO.setVehicleToBeRented(vehicleToBeRented); //assign vehicle with no image
            rentalShowDTO.setCustomerUsername(null); //remove customer information.

            rentalShowDTO.calculateTimeLeftForRental();
            rentalShowDTOS.add(rentalShowDTO);
        }
        return rentalShowDTOS;
    }

    @Override
    public HashMap<String, Integer> countCustomerPendingRentals(String username) throws DataFormatException, IOException, ResourceNotFoundException {
        List<Rental> allCustomerCanBeCollectedRentals = rentalRepository.getAllByIsApprovedEqualsAndTheCustomerRentingEquals(
                null, userService._getUserWithImageDecompression(username), null
        );

        HashMap<String, Integer> pendingCount = new HashMap<>();
        pendingCount.put("count", allCustomerCanBeCollectedRentals.size());
        return pendingCount;
    }

    @Override
    public HashMap<String, Integer> countCustomerPastRentals(String username) throws DataFormatException, IOException, ResourceNotFoundException {
        List<Rental> allPastRentals = rentalRepository.getAllByIsApprovedEqualsAndIsCollectedEqualsAndIsReturnedEqualsAndTheCustomerRentingEquals(
                true, true, true, userService._getUserWithImageDecompression(username), null
        );

        HashMap<String, Integer> pastCount = new HashMap<>();
        pastCount.put("count", allPastRentals.size());
        return pastCount;
    }

    @Override
    public HashMap<String, Integer> countCustomerRejectedRentals(String username) throws DataFormatException, IOException, ResourceNotFoundException {
        List<Rental> rejectedRentalsCustomer = rentalRepository.getAllByIsApprovedEqualsAndTheCustomerRentingEquals(
                false, userService._getUserWithImageDecompression(username), null
        );

        HashMap<String, Integer> rejectedCount = new HashMap<>();
        rejectedCount.put("count", rejectedRentalsCustomer.size());
        return rejectedCount;
    }

    /**
     * Method will update the rental return time upto 4pm. New rental cost and equipment costs will be calculated.
     *
     * @param theUpdateRentalDTO The rental to be updated
     */
    @Override
    public void updateRentalReturnTime(RentalCreateDTO theUpdateRentalDTO) throws ResourceNotFoundException, ParseException, BadValuePassedException, ResourceNotUpdatedException {
        Rental rentalToBeUpdatedFromDB = rentalRepository.findById(theUpdateRentalDTO.getRentalId()).orElseThrow(() -> new ResourceNotFoundException("The rental you are trying to update does not exist at Banger and Co."));

        //before updating, check if initially set return time is after 4pm
        //if so, cannot update.
        if (LocalTime.parse(theUpdateRentalDTO.getReturnTime()).isAfter(LocalTime.of(16, 0))) {
            //cannot update
            throw new BadValuePassedException("This rental can only be extended upto 4pm");
        }

        //check if new return time < current return so, if so do not allow
        if (rentalToBeUpdatedFromDB.getReturnTime().isAfter(LocalTime.parse(theUpdateRentalDTO.getReturnTime()))) {
            //newly set return time is less than current return time
            throw new BadValuePassedException("You can only extend your time duration. You are not allowed to decrease the time on rental");
        }


        VehicleRentalFilterDTO clientNewDate = new VehicleRentalFilterDTO();
        clientNewDate.setPickupDate(new SimpleDateFormat("yyyy-MM-dd").parse(theUpdateRentalDTO.getPickupDate()));
        clientNewDate.setReturnDate(new SimpleDateFormat("yyyy-MM-dd").parse(theUpdateRentalDTO.getReturnDate()));
        clientNewDate.setPickupTime(LocalTime.parse(theUpdateRentalDTO.getPickupTime()));
        clientNewDate.setReturnTime(LocalTime.parse(theUpdateRentalDTO.getReturnTime()));

        LocalDateTime pickupDateTime = LocalDateTime.of(clientNewDate.getPickupDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), clientNewDate.getPickupTime());
        LocalDateTime returnDateTime = LocalDateTime.of(clientNewDate.getReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), clientNewDate.getReturnTime());

        //check if duration is maximum of 2 weeks and start and end time is between 8 and 18:00
        //if same day rental, check if minimum of 5 hours is present.
        validateRentalFilters(clientNewDate);
        //date and time is valid.

        //check if vehicle is free on given duration.
        //check if there are rentals from new return time to next day midnight
        List<Rental> theRentalsFromReturnDate = rentalRepository.findRentalsFromToday(rentalToBeUpdatedFromDB.getReturnDate());

        LocalDate dayAfter = rentalToBeUpdatedFromDB.getReturnDate().plus(1, ChronoUnit.DAYS);

        for (Rental eachRental : theRentalsFromReturnDate) {
            boolean isRejected = eachRental.getApproved() != null && !eachRental.getApproved();
            boolean isReturned = eachRental.getReturned() != null && eachRental.getReturned();

            if (!isReturned && !isRejected) {
                //validate only for rentals that are not rejected and not returned: PENDING,APPROVED,ONGOING
                if (rentalToBeUpdatedFromDB.getVehicleOnRental().getVehicleId() == eachRental.getVehicleOnRental().getVehicleId()) {
                    //when updating, do not check next day rentals for other vehicles. check only for the vehicle being updated.
                    if (dayAfter.equals(eachRental.getPickupDate())) {
                        //day after - 22nd
                        //pickup - 22nd - 22nd = 22nd = day after rental

                        //pickup is day after the return date
                        throw new ResourceNotUpdatedException("This vehicle has a rental for tomorrow therefore your rental cannot be extended");
                    } else if (eachRental.getPickupDate().equals(rentalToBeUpdatedFromDB.getReturnDate())) {
                        //pickup date for db rental is same day as rental return date for update
                        //check if it is the same rental as updating one
                        if (eachRental.getRentalId() != rentalToBeUpdatedFromDB.getRentalId()) {
                            //different rental, proceed
                            //check if pickup time is after or equal new time
                            if (clientNewDate.getReturnTime().isAfter(eachRental.getPickupTime()) || clientNewDate.getReturnTime().equals(eachRental.getPickupTime())) {
                                //check if new return time is greater than or equals to the pickup time in DB
                                throw new ResourceNotUpdatedException("This vehicle has a rental on the new return time you specified");
                            }
                        }
                    }
                }
            }
        }

        //re-calculate cost
        Vehicle theVehicle = rentalToBeUpdatedFromDB.getVehicleOnRental();
        long rentalPeriodInHours = pickupDateTime.until(returnDateTime, ChronoUnit.HOURS);
        double totalCostForVehicle = calculateCostForVehicle(rentalPeriodInHours, theVehicle);

        double costOfEquipments = 0;
        for (RentalCustomization eachCustomization : rentalToBeUpdatedFromDB.getRentalCustomizationList()) {
            double pricePerDay = eachCustomization.getEquipmentAddedToRental().getPricePerDay();
            double costOfEachEquipment;
            costOfEachEquipment = ((pricePerDay / PRICE_PER_DAY_DIVISOR) * rentalPeriodInHours) * eachCustomization.getQuantityAddedForEquipmentInRental();
            costOfEquipments = costOfEquipments + costOfEachEquipment;
            eachCustomization.setTotalPriceForEquipment(costOfEachEquipment);
        }

        rentalToBeUpdatedFromDB.setReturnTime(LocalTime.parse(theUpdateRentalDTO.getReturnTime()));
        rentalToBeUpdatedFromDB.setTotalCost(totalCostForVehicle + costOfEquipments);
        rentalToBeUpdatedFromDB.setReturnTime(LocalTime.parse(theUpdateRentalDTO.getReturnTime()));


        rentalRepository.save(rentalToBeUpdatedFromDB);
    }

    /**
     * Method will allow updating rental addons till the rental has been collected.
     *
     * @param theAddOnDTO The new equipment set
     */
    @Override
    @Transactional
    public void updateRentalAddOns(RentalAdditionalEquipmentUpdateDTO theAddOnDTO) throws ResourceNotFoundException, ResourceNotUpdatedException, ResourceNotCreatedException {
        Rental rentalToBeCustomized = rentalRepository.findById(theAddOnDTO.getRental()).orElseThrow(() -> new ResourceNotFoundException("The rental that you wish to customize does not exist at Banger and Co."));
        LocalDateTime pickupDateTime = LocalDateTime.of(rentalToBeCustomized.getPickupDate(), rentalToBeCustomized.getPickupTime());
        LocalDateTime returnDateTime = LocalDateTime.of(rentalToBeCustomized.getReturnDate(), rentalToBeCustomized.getReturnTime());

        //if rental has been collected, do not allow
        if (rentalToBeCustomized.getCollected() != null && rentalToBeCustomized.getCollected()) {
            throw new ResourceNotUpdatedException("This rental has already been collected. Therefore, you cannot customize this rental");
        }
        //initially start by adding the current quantity back to the stock and starting new if equipments are present
        if (rentalToBeCustomized.getRentalCustomizationList() != null) {
            for (RentalCustomization eachCustomization : rentalToBeCustomized.getRentalCustomizationList()) {
                additionalEquipmentService.addQuantityBackToItem(eachCustomization);
            }
            //delete existing rental customizations from the database and start new.
            rentalCustomizationService.deleteByRental(rentalToBeCustomized);
        }

        //re-calculate totals and get new equipment list while updating quantity back into addons.
        long rentalPeriodInHours = pickupDateTime.until(returnDateTime, ChronoUnit.HOURS);

        double vehicleCost = calculateCostForVehicle(rentalPeriodInHours, rentalToBeCustomized.getVehicleOnRental());
        RentalEquipmentCalculatorSupporter equipmentsAndTotalPriceForEquipments = getEquipmentsAndTotalPriceForEquipments(
                theAddOnDTO.getUpdateEquipments(), rentalPeriodInHours, rentalToBeCustomized
        );
        rentalToBeCustomized.setTotalCost(vehicleCost + equipmentsAndTotalPriceForEquipments.getTotalCostForAdditionalEquipment());
        rentalToBeCustomized.setRentalCustomizationList(equipmentsAndTotalPriceForEquipments.getRentalCustomizationList());

        rentalRepository.save(rentalToBeCustomized);
    }

    private List<ChartReturn> fillEmptyMonths(List<ChartReturn> formattedDBData, Calendar dateTime12MonthsAgo) {
        List<String> monthList = new ArrayList<>();
        DateFormatSymbols monthProvider = new DateFormatSymbols(); //used to provide month names
        String[] monthArray = monthProvider.getMonths();  //get months will return the list of months
        while (dateTime12MonthsAgo.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            //fill up months from 12 months ago till current time
            //passing the index will return the month to add
            monthList.add(monthArray[dateTime12MonthsAgo.get(Calendar.MONTH)].toUpperCase() + " - " + dateTime12MonthsAgo.get(Calendar.YEAR));
            dateTime12MonthsAgo.add(Calendar.MONTH, 1); //add one more month
        }

        for (String eachMonth : monthList) {
            int monthInReturn = isMonthInReturn(eachMonth, formattedDBData);
            if (monthInReturn == -1) {
                //-1 = not present
                //insert empty data to fill past 12 month gap, if not present
                formattedDBData.add(new ChartReturn(eachMonth));
            }
        }

        return formattedDBData;
    }

    private List<ChartReturn> compileInToMonthsAndCount(List<Rental> completedPast12MonthsRentals) throws Exception {
        List<ChartReturn> chartData = new ArrayList<>();

        for (Rental eachRental : completedPast12MonthsRentals) {
            String monthName = eachRental.getPickupDate().getMonth().toString();
            int year = eachRental.getPickupDate().getYear();
            monthName = monthName + " - " + year; //combine month and year

            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO vehicleToBeRented = rentalShowDTO.getVehicleToBeRented();
            if (vehicleToBeRented != null) {
                vehicleToBeRented.setVehicleImage(null); //remove vehicle image
            }

            UserDTO customerRenting = rentalShowDTO.getCustomerUsername();
            customerRenting.setLicensePic(null);
            customerRenting.setOtherIdentity(null);
            customerRenting.setProfilePicture(null); //remove images from customer as well

            rentalShowDTO.setVehicleToBeRented(vehicleToBeRented); //assign vehicle with no image
            rentalShowDTO.setCustomerUsername(customerRenting); //assign customer with no images.

            int isPresent = isMonthInReturn(monthName, chartData);

            if (isPresent != -1) {
                //month already added to list

                ChartReturn chartReturn = chartData.get(isPresent);

                chartReturn.setCount(chartReturn.getCount() + 1); //update the count for the month
                chartReturn.addRental(rentalShowDTO); //add the new rental to the array

                chartData.set(isPresent, chartReturn); //update the data for the existing month
            } else {
                //month is new
                ChartReturn chartReturn = new ChartReturn();
                chartReturn.setMonth(monthName);
                chartReturn.setCount(1);
                chartReturn.addRental(rentalShowDTO);

                chartData.add(chartReturn); //insert the data for new month
            }
        }

        return chartData;
    }

    private int isMonthInReturn(String month, List<ChartReturn> checker) {
        int index = 0;
        for (ChartReturn eachCheck : checker) {
            if (eachCheck.getMonth().equals(month)) {
                return index;
            }
            index = index + 1;
        }
        return -1; //not found
    }

    /**
     * Method will get a list of all pending rentals from the database.
     * A sort will be done to retrieve the vehicles with the soonest pickup date to be in the first.
     *
     * @return - The list of vehicles that are pending.
     */
    @Override
    public HashMap<String, Object> getAllPendingRentals(int pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();
        //sort the rentals to get the vehicles that need to be picked up earliest.

        Pageable thePage = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("pickupDate").ascending());
        //all pending = isApproved = NULL
        List<Rental> allPendingRentals = rentalRepository.getAllByIsApprovedEquals(
                thePage
        );

        List<RentalShowDTO> theRentalList = new ArrayList<>();

        for (Rental eachRental : allPendingRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }

            UserDTO theCustomer = rentalShowDTO.getCustomerUsername();
            theCustomer.setLicensePic(null);
            theCustomer.setOtherIdentity(null);
            theCustomer.setProfilePicture(null);

            rentalShowDTO.setCustomerUsername(theCustomer);

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theRentalList.add(rentalShowDTO);
        }

        returnList.put("nextPageNumber", pageNumber + 1);
        returnList.put("thePendingRentals", theRentalList);

        return returnList;
    }

    private RentalShowDTO convertToDTO(Rental eachRental) throws Exception {
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
        if (eachRental.getVehicleOnRental() != null) {
            theDTO.setVehicleToBeRented(vehicleService.getVehicleById(eachRental.getVehicleOnRental().getVehicleId()));
        }
        theDTO.setEquipmentsAddedToRental(additionalEquipmentService.getEquipmentForRental(eachRental.getRentalCustomizationList()));
        theDTO.setCustomerUsername(userService.getUserInformation(eachRental.getTheCustomerRenting().getUsername()));

        return theDTO;
    }

    /**
     * Method will blacklist customers if they have a rental that they have not picked up.
     * All blacklisted customers will be notified via an email informing about their account being blacklisted.
     * <br>
     * <b>Business Rule: </b> If the rental has been approved and has not been collected even after the day of return, the user will get blacklisted.
     */
    @Override
    @Transactional
    public void blacklistCustomers() throws ResourceNotFoundException {
        List<User> blacklistedUsers = new ArrayList<>();
        List<Rental> allRentalsThatHavePassedReturnDate = rentalRepository.getAllRentalsThatHavePassedReturnDate(
                LocalDate.now(), true, false);

        for (Rental eachRental : allRentalsThatHavePassedReturnDate) {
            if (LocalDateTime.now().isAfter(LocalDateTime.of(eachRental.getReturnDate(), eachRental.getReturnTime()))) {
                User theCustomerNotCollected = eachRental.getTheCustomerRenting();
                List<RentalCustomization> rentalCustomizationList = eachRental.getRentalCustomizationList();

                for (RentalCustomization eachCustomization : rentalCustomizationList) {
                    additionalEquipmentService.addQuantityBackToItem(eachCustomization);
                }
                userService.blackListCustomer(theCustomerNotCollected.getUsername(), eachRental);
                blacklistedUsers.add(theCustomerNotCollected);
                rentalRepository.delete(eachRental);
            }
        }

//        send an email to all the administrators in the system regarding the blacklisted customers if there are any
        if (blacklistedUsers.size() > 0) {
            List<String> adminList = userService._getAllAdminEmails();
            try {
                mailSender.sendBulkRentalEmails(
                        adminList, "Blacklist Job Report", blacklistedUsers, MailTemplateType.ADMIN_BULK_BLACKLIST, null
                );
            } catch (Exception ex) {
                LOGGER.warning("EMAIL NOT SENT DURING BLACKLIST");
            }
        }
    }

    /**
     * Method will return the return for the passed rental id from the database
     *
     * @param rentalId The rental to get detailed information for
     * @return The rental object from the database.
     */
    @Override
    public RentalShowDTO getRentalById(Integer rentalId) throws Exception {
        Rental theRental = rentalRepository.findById(rentalId).orElseThrow(() -> new ResourceNotFoundException("The rental you are trying to find does not exist at Banger and Co."));
        RentalShowDTO rentalShowDTO = convertToDTO(theRental);

        //the `convertToDTO` method does not retrieve the license and other image
        //therefore manually fetch it by calling the methods in the user service and the set it to the user object in the rental dto
        UserDTO theUserOnRental = rentalShowDTO.getCustomerUsername();
        theUserOnRental.setLicensePic(userService.getCustomerLicenseImage(theUserOnRental.getUsername()));
        theUserOnRental.setOtherIdentity(userService.getCustomerOtherImage(theUserOnRental.getUsername()));

        rentalShowDTO.setCustomerUsername(theUserOnRental); //assign the user with the two identity images
        return rentalShowDTO;
    }

    /**
     * Method will reject the rental and will email the customer stating their rental was rejected.
     *
     * @param rentalId       The rental to reject.
     * @param rejectedReason The reason the rental was rejected.
     */
    @Override
    public void rejectRental(Integer rentalId, String rejectedReason) throws ResourceNotFoundException, BadValuePassedException {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new ResourceNotFoundException("The rental you are trying to reject does not exist at Banger and Co."));
        //if the rental has already been rejected, do not reject it again.
        if (rental.getApproved() != null) {
            //rental is already handled
            throw new BadValuePassedException("The rental you are trying to reject has already been handled");
        } else {
            User theCustomerRenting = rental.getTheCustomerRenting();
            rental.setApproved(false); //reject the rental.
            //update the rejected quantity back into the database of additional items

            for (RentalCustomization eachAddOn : rental.getRentalCustomizationList()) {
                additionalEquipmentService.addQuantityBackToItem(eachAddOn);
            }

            rentalRepository.save(rental); //update the rental status in the database.

            //email the client to indicate that their rental has been rejected.
            try {
                mailSender.sendRentalMail(
                        new MailSenderHelper(theCustomerRenting, "Rental Has Been Rejected", MailTemplateType.RENTAL_REJECTED, rejectedReason),
                        rental
                );
            } catch (Exception ex) {
                LOGGER.warning("ERROR SENDING REJECT EMAIL");
            }
        }
    }

    /**
     * Method will approve the rental and will inform the customer that their rental was approved via an email.
     *
     * @param rentalId The rental to approve.
     */
    @Override
    public void approveRental(Integer rentalId) throws ResourceNotFoundException, BadValuePassedException {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new ResourceNotFoundException("The rental you are trying to approve does not exist at Banger and Co."));

        if (rental.getApproved() != null) {
            //rental was already handled
            throw new BadValuePassedException("The rental you are trying to approve has already been handled");
        } else {
            User theCustomerRenting = rental.getTheCustomerRenting();

            if (theCustomerRenting.isBlackListed()) {
                throw new BadValuePassedException("The rental cannot be approved because this customer is blacklisted");
            }
            //approve the rental
            //when approving -> isApproved is TRUE and isCollected is FALSE to indicate the rental has been approved but not yet been collected.
            rental.setApproved(true);
            rental.setCollected(false);
            rentalRepository.save(rental); //save the changes in the database.

            //send an email to the customer indicating that their vehicle can be picked up
            try {
                mailSender.sendRentalMail(
                        new MailSenderHelper(
                                theCustomerRenting, "Rental Has Been Approved", MailTemplateType.RENTAL_APPROVED
                        ),
                        rental
                );
            } catch (Exception ex) {
                LOGGER.warning("APPROVE EMAIL NOT SENT");
            }
        }
    }

    /**
     * Method returns all the pending rentals for the customer. <br>
     * isApproved - NULL
     *
     * @param username The customer to get the rental information for
     * @return The object containing the next page token and the pending rentals for the customer.
     */
    @Override
    public HashMap<String, Object> getCustomerPendingRentals(String username, int pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();

        User theCustomer = userService._getUserWithoutDecompression(username);
        Pageable thePaginator = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("pickupDate").ascending());

        //null - pending rental
        List<Rental> allPendingCustomerRentals = rentalRepository.getAllByIsApprovedEqualsAndTheCustomerRentingEquals(
                null, theCustomer, thePaginator
        );

        List<RentalShowDTO> theReturnDTOList = new ArrayList<>();
        for (Rental eachRental : allPendingCustomerRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }
            rentalShowDTO.setCustomerUsername(null); //no need customer information as the customer renting the vehicle is requesting data.

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theReturnDTOList.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1); //next page to query data from.
        returnList.put("customerPendingRentals", theReturnDTOList);

        return returnList;
    }

    /**
     * Method returns all the rentals that have been approved and can be collected from Banger and Co. <br>
     * isApproved - TRUE & isCollected - FALSE
     *
     * @param username   The customer to get the can be collected rentals to
     * @param pageNumber The page number to get paginated data
     * @return The object containing the list of can be collected vehicles and the next page token.
     */
    @Override
    public HashMap<String, Object> getCustomerCanBeCollectedRentals(String username, Integer pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();

        User theCustomer = userService._getUserWithoutDecompression(username);
        Pageable thePaginator = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("pickupDate").ascending());

        //true - approved rental, false - not yet collected.
        //when rental starts - isCollected - true & isReturned - false.
        List<Rental> allCustomerCanBeCollectedRentals = rentalRepository.getAllByIsApprovedEqualsAndIsCollectedEqualsAndTheCustomerRentingEquals(
                true, false, theCustomer, thePaginator
        );

        List<RentalShowDTO> theReturnDTOList = new ArrayList<>();
        for (Rental eachRental : allCustomerCanBeCollectedRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }
            rentalShowDTO.setCustomerUsername(null); //no need customer information as the customer renting the vehicle is requesting data.

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theReturnDTOList.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1); //next page to query data from.
        returnList.put("customerCanBeCollectedRentals", theReturnDTOList);

        return returnList;
    }

    /**
     * Method will return a list of the rentals of the customer that have been returned (PAST RENTAL HISTORY) <br>
     * isCollected - TRUE & isReturned - TRUE
     *
     * @param username   The customer to get the past rentals for
     * @param pageNumber The page information
     * @return The object containing next page information and the past rentals.
     */
    @Override
    public HashMap<String, Object> getCustomerCompletedRentals(String username, Integer pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();

        User theCustomer = userService._getUserWithoutDecompression(username);
        Pageable thePaginator = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("returnDate").descending());

        List<Rental> allCustomerCompletedRentals = rentalRepository.getAllByIsApprovedEqualsAndIsCollectedEqualsAndIsReturnedEqualsAndTheCustomerRentingEquals(
                true, true, true, theCustomer, thePaginator
        );

        List<RentalShowDTO> theReturnDTOList = new ArrayList<>();
        for (Rental eachRental : allCustomerCompletedRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }
            rentalShowDTO.setCustomerUsername(null); //no need customer information as the customer renting the vehicle is requesting data.

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theReturnDTOList.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1); //next page to query data from.
        returnList.put("customerCompletedRentals", theReturnDTOList);

        return returnList;
    }

    /**
     * Method will get a list of on-going rentals for the customer <br>
     * isApproved - TRUE & isCollected - TRUE & isReturned - FALSE
     *
     * @param username   The customer to get the on going rentals for
     * @param pageNumber The page number
     * @return The list of on going rentals
     * @throws Exception Thrown during image de-compression
     */
    @Override
    public HashMap<String, Object> getCustomerOnGoingRentals(String username, Integer pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();

        User theCustomer = userService._getUserWithoutDecompression(username);
        Pageable thePaginator = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("returnDate").descending());

        //is returned - false & isCollected - true means rental is ongoing
        List<Rental> allOnGoingCustomerRentals = rentalRepository.getAllByIsApprovedEqualsAndIsCollectedEqualsAndIsReturnedEqualsAndTheCustomerRentingEquals(
                true, true, false, theCustomer, thePaginator
        );

        List<RentalShowDTO> theReturnDTOList = new ArrayList<>();
        for (Rental eachRental : allOnGoingCustomerRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }
            rentalShowDTO.setCustomerUsername(null); //no need customer information as the customer renting the vehicle is requesting data.

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theReturnDTOList.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1); //next page to query data from.
        returnList.put("customerOnGoingRentals", theReturnDTOList);

        return returnList;
    }

    /**
     * Method will get a list of on-going rentals for the customer.
     * <br> isApproved - FALSE
     *
     * @param username   The customer to get the rejected rentals for
     * @param pageNumber The page number
     * @return The list of rejected rentals along with the next page token.
     */
    @Override
    public HashMap<String, Object> getCustomerRejectedRentals(String username, Integer pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();

        User theCustomer = userService._getUserWithoutDecompression(username);
        Pageable thePaginator = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("pickupDate").descending());

        //isApproved - false - rejected rental
        List<Rental> allPendingCustomerRentals = rentalRepository.getAllByIsApprovedEqualsAndTheCustomerRentingEquals(
                false, theCustomer, thePaginator
        );

        List<RentalShowDTO> theReturnDTOList = new ArrayList<>();
        for (Rental eachRental : allPendingCustomerRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }
            rentalShowDTO.setCustomerUsername(null); //no need customer information as the customer renting the vehicle is requesting data.

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theReturnDTOList.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1); //next page to query data from.
        returnList.put("customerRejectedRentals", theReturnDTOList);

        return returnList;
    }

    /**
     * Method returns a list of the rejected rentals at Banger and Co.
     *
     * @param pageNumber The page number to get the data for
     * @return The object containing the rejected rentals and the next page number.
     */
    @Override
    public HashMap<String, Object> getAllRejectedRentals(Integer pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();
        Pageable theNextPage = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("pickupDate").descending());
        List<Rental> rejectedRentals = rentalRepository.getAllRejectedRentals(theNextPage);

        List<RentalShowDTO> theRejectedDTOList = new ArrayList<>();

        for (Rental eachRental : rejectedRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }

            UserDTO theCustomer = rentalShowDTO.getCustomerUsername();
            theCustomer.setLicensePic(null);
            theCustomer.setOtherIdentity(null);
            theCustomer.setProfilePicture(null);

            rentalShowDTO.setCustomerUsername(theCustomer);

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theRejectedDTOList.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1);
        returnList.put("rejectedRentals", theRejectedDTOList);

        return returnList;
    }

    /**
     * Method will get a list of all the approved rentals that can be collected from Banger and Co.
     *
     * @param pageNumber The page number to query the data for
     * @return The object consisting of all the approved rentals along with the next page token
     */
    @Override
    public HashMap<String, Object> getAllApprovedRentals(Integer pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();
        //sort by ascending pickup date so the vehicle soonest to get picked up is shown on top.
        Pageable nextPage = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("pickupDate").ascending());
        //when rental is approved and is collected is false, the rental can be collected.
        List<Rental> rentalList = rentalRepository.getAllCanBeCollectedRentals(true, false, nextPage);

        List<RentalShowDTO> theApprovedRentalList = new ArrayList<>();

        for (Rental eachRental : rentalList) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }

            UserDTO theCustomer = rentalShowDTO.getCustomerUsername();
            theCustomer.setLicensePic(null);
            theCustomer.setOtherIdentity(null);
            theCustomer.setProfilePicture(null);

            rentalShowDTO.setCustomerUsername(theCustomer);

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            theApprovedRentalList.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1);
        returnList.put("approvedRentals", theApprovedRentalList);

        return returnList;
    }

    /**
     * Method will get a list of all on-going rentals for the given page number
     *
     * @param pageNumber The page number to get the data for
     * @return The list containing the on-going rentals and the next page number.
     */
    @Override
    public HashMap<String, Object> getAllOnGoingRentals(Integer pageNumber) throws Exception {
        HashMap<String, Object> returnList = new HashMap<>();
        Pageable theNextPage = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("returnDate").ascending());

        List<Rental> allOnGoingRentals = rentalRepository.getAllOnGoingRentals(theNextPage);

        List<RentalShowDTO> allOnGoingDTOs = new ArrayList<>();

        for (Rental eachRental : allOnGoingRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }

            UserDTO theCustomer = rentalShowDTO.getCustomerUsername();
            theCustomer.setLicensePic(null);
            theCustomer.setOtherIdentity(null);
            theCustomer.setProfilePicture(null);

            rentalShowDTO.setCustomerUsername(theCustomer);

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            allOnGoingDTOs.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1);
        returnList.put("allOnGoingRentals", allOnGoingDTOs);
        return returnList;
    }

    /**
     * Method will return a list of all completed/past rentals at Banger and Co.
     *
     * @param pageNumber The page number to get the data for
     * @return The list containing all past rentals and the next page number.
     */
    @Override
    public HashMap<String, Object> getAllCompletedRentals(Integer pageNumber) throws Exception {
        //get with most recent returned rental on first.
        Pageable theNextPage = PageRequest.of(pageNumber, ITEMS_PER_PAGE, Sort.by("returnDate").descending());
        HashMap<String, Object> returnList = new HashMap<>();
        List<RentalShowDTO> returnDTOList = new ArrayList<>();

        List<Rental> completedRentals = rentalRepository.getAllCompletedRentals(theNextPage);

        for (Rental eachRental : completedRentals) {
            RentalShowDTO rentalShowDTO = convertToDTO(eachRental);

            VehicleShowDTO theVehicleToBeShown = rentalShowDTO.getVehicleToBeRented();
            if (theVehicleToBeShown != null) {
                theVehicleToBeShown.setVehicleImage(null); //initially dont add vehicle image to return.
            }

            UserDTO theCustomer = rentalShowDTO.getCustomerUsername();
            theCustomer.setLicensePic(null);
            theCustomer.setOtherIdentity(null);
            theCustomer.setProfilePicture(null);

            rentalShowDTO.setCustomerUsername(theCustomer);

            rentalShowDTO.setVehicleToBeRented(theVehicleToBeShown);
            returnDTOList.add(rentalShowDTO);
        }

        returnList.put("nextPage", pageNumber + 1);
        returnList.put("allCompleted", returnDTOList);

        return returnList;
    }

    /**
     * Method will start the rental if it exists and has not been started before and if the customer is not blacklisted
     * <br>
     * When collecting - isApproved - true && isCollected - true && isReturned - false
     *
     * @param rentalId The rental to start
     */
    @Override
    public void startRental(Integer rentalId) throws Exception {
        Rental theRentalToBeStarted = rentalRepository.findById(rentalId).orElseThrow(() -> new ResourceNotFoundException("The rental that you are trying to start does not exist at Banger and Co."));
        if (theRentalToBeStarted.getApproved() == null || !theRentalToBeStarted.getApproved()) {
            throw new ResourceNotUpdatedException("This rental has been rejected therefore it cannot be started");
        }

        User theCustomerRenting = theRentalToBeStarted.getTheCustomerRenting();

        //does customer have any other on-going rentals, if so, cannot start
        List<RentalShowDTO> customerOnGoingRentals = getCustomerOnGoingRentals(theCustomerRenting.getUsername());

        if (customerOnGoingRentals.size() > 0) {
            throw new ResourceNotUpdatedException("This customer already has on-going rentals, therefore, this new rental cannot be started until previous on-going rentals are returned");
        }

        if (theRentalToBeStarted.getCollected() != null && !theRentalToBeStarted.getCollected()) {
            //rental has not yet been collected
            if (theCustomerRenting.isBlackListed()) {
                //customer is blacklisted, do not allow rental to be started.
                throw new ResourceNotUpdatedException("The rental could not be started because this customer is already blacklisted. Whitelist the customer to start this rental");
            }
            //when collecting - isCollected = true && isReturned = false
            theRentalToBeStarted.setCollected(true);
            theRentalToBeStarted.setReturned(false);

            Rental startedRental = rentalRepository.save(theRentalToBeStarted);//update rental information to indicate it is collected, and not yet returned
            try {
                mailSender.sendRentalMail(
                        new MailSenderHelper(startedRental.getTheCustomerRenting(), "Rental Has Started", MailTemplateType.RENTAL_STARTED),
                        startedRental
                );
            } catch (IOException | MessagingException e) {
                LOGGER.warning("ERROR SENDING RENTAL START EMAIL");
            }
        } else {
            //the rental is either pending or has been collected.
            throw new BadValuePassedException("The rental you are trying to start is currently pending, or has been collected already");
        }
    }

    private boolean checkForFraudulentClaims(User theCustomerRenting) throws ResourceNotFoundException {
        List<FraudClient> fraudulentClaimsForCustomer = this.communicateWithInsurersDatabase(theCustomerRenting);
        //have fraudulent claims
        return fraudulentClaimsForCustomer.size() > 0;
    }


    /**
     * Method will trigger a call to a stored procedure name "IS_USER_FRADULENT".
     * <br>
     * This stored procedure will then communicate to the insurer database by using an SQL View to get the fraud data for the given license.
     * <br>
     * This client data is then returned back to the client via the automatic row mapping of BeanPropertyRowMapper.
     *
     * @param theCustomer The customer to check if they have fradulent claims
     * @return The list of fradulent claims for the client.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<FraudClient> communicateWithInsurersDatabase(User theCustomer) throws ResourceNotFoundException {
        try {
            String drivingLicenseNumber = theCustomer.getDrivingLicenseNumber();
            //create the inputs required for the stored procedure.
            SqlParameterSource theInputParameters = new MapSqlParameterSource()
                    .addValue(INPUT_PARAM_STORED_PROCEDURE, drivingLicenseNumber);

            Map<String, Object> theExecutedResult = isUserFraudulentProcedureCall.execute(theInputParameters);
            //retrieve the data returned from stored procedure mapped via BeanPropertyRowMapper.
            return (List<FraudClient>) theExecutedResult.get(JDBC_RESULT_CARRIER);
        } catch (Exception ex) {
            //exception occurred, stop app flow as this check is a must.
            throw new ResourceNotFoundException("The Insurer Database Could Not Be Reached For Customer Fraud Validation");
        }
    }

    /**
     * Method will reject rental once customer has fraudulent claims
     *
     * @param theRentalToBeStarted The rental to reject
     * @param theCustomerRenting   The customer with fraudulent claims
     * @param rejectReason         The reason for rejection.
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void rejectRental(Rental theRentalToBeStarted, User theCustomerRenting, String rejectReason) {
        List<RentalCustomization> rentalCustomizationList = theRentalToBeStarted.getRentalCustomizationList();
        for (RentalCustomization eachCustomization : rentalCustomizationList) {
            //add the items back for due to rejection
            additionalEquipmentService.addQuantityBackToItem(eachCustomization);
        }

        //reject the rental
        theRentalToBeStarted.setApproved(false);
        theRentalToBeStarted.setCollected(null);
        theRentalToBeStarted.setReturned(null);

        Rental rejectedRental = rentalRepository.save(theRentalToBeStarted);

        try {
            mailSender.sendRentalMail(
                    new MailSenderHelper(theCustomerRenting, "Rental Has Been Rejected", MailTemplateType.RENTAL_REJECTED, rejectReason),
                    rejectedRental
            );
        } catch (Exception ex) {
            LOGGER.warning("ERROR SENDING REJECT EMAIL");
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
     * @param theRentalToBeMade       The rental that is going to be created
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
                AdditionalEquipment item = additionalEquipmentService._getAdditionalEquipmentById(eachEquipment.getEquipmentId());
                //calculate total price for rental and reduce equipment quantity as rental is made.

                if (eachEquipment.getQuantitySelectedForRental() > item.getEquipmentQuantity()) {
                    throw new ResourceNotCreatedException("The equipment: " + eachEquipment.getEquipmentName() + " has no " + eachEquipment.getQuantitySelectedForRental() + " in stock");
                }

                item.setEquipmentQuantity(item.getEquipmentQuantity() - eachEquipment.getQuantitySelectedForRental());

                RentalCustomization eachCustomization = new RentalCustomization();
                eachCustomization.setTheRentalInformation(theRentalToBeMade);
                eachCustomization.setEquipmentAddedToRental(item);
                eachCustomization.setQuantityAddedForEquipmentInRental(eachEquipment.getQuantitySelectedForRental());

                //calculate the price per hour by getting price per day / 24
                double pricePerHourForEachItem = item.getPricePerDay() / PRICE_PER_DAY_DIVISOR;

                //calculate total price for equipment by price per hour * period in hours * quantity added to rental
                double costForEachEquipment = pricePerHourForEachItem * rentalPeriodInHours * eachEquipment.getQuantitySelectedForRental();

                //set the total price for the entity
                eachCustomization.setTotalPriceForEquipment(costForEachEquipment); //set the total price of each equipment
                addedCustomization.add(eachCustomization); //add it to the equipments array added to the rental.

                totalPriceForEquipments += costForEachEquipment; //get the total sum

            }
        }
        supporter.setRentalCustomizationList(addedCustomization);
        supporter.setTotalCostForAdditionalEquipment(totalPriceForEquipments);
        return supporter;
    }
}
