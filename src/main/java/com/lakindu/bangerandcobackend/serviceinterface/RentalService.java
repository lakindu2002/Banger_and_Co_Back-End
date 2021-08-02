package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.ChartReturn;
import com.lakindu.bangerandcobackend.dto.RentalCreateDTO;
import com.lakindu.bangerandcobackend.dto.RentalShowDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotCreatedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

public interface RentalService {
    /**
     * Validates the passed filters according to the required criteria.
     * Minimum Rental Period: 5 Hours
     * Maximum Rental Period: 2 Weeks
     * Rental Pickup & Return Time: 8:00am to 6:00pm
     *
     * @param theFilterDTO The filtering object to validate with business rules
     * @throws BadValuePassedException The exception thrown when the filter does not meet business criteria
     * @author Lakindu Hewawasam
     */
    void validateRentalFilters(VehicleRentalFilterDTO theFilterDTO) throws BadValuePassedException;

    void makeRental(RentalCreateDTO theRental) throws ParseException, BadValuePassedException, ResourceNotFoundException, ResourceNotCreatedException;

    /**
     * Method will get a list of all pending rentals from the database.
     * A sort will be done to retrieve the vehicles with the soonest pickup date to be in the first.
     *
     * @param pageNumber The page number to query data for.
     * @return - The list of vehicles that are pending and the next page number token.
     */
    HashMap<String, Object> getAllPendingRentals(int pageNumber) throws Exception;

    /**
     * Method will blacklist customers if they have a rental that they have not picked up.
     * All blacklisted customers will be notified via an email informing about their account being blacklisted.
     * <br>
     * <b>Business Rule: </b> If the rental has been approved and has not been collected even after the day of return, the user will get blacklisted.
     */
    void blacklistCustomers() throws ResourceNotFoundException;

    /**
     * Method will return the return for the passed rental id from the database
     *
     * @param rentalId The rental to get detailed information for
     * @return The rental object from the database.
     */
    RentalShowDTO getRentalById(Integer rentalId) throws Exception;

    /**
     * Method will reject the rental and will email the customer stating their rental was rejected.
     *
     * @param rentalId       The rental to reject.
     * @param rejectedReason The reason the rental was rejected.
     */
    void rejectRental(Integer rentalId, String rejectedReason) throws ResourceNotFoundException, BadValuePassedException;

    /**
     * Method will approve the rental and will inform the customer that their rental was approved via an email.
     *
     * @param rentalId The rental to approve.
     */
    void approveRental(Integer rentalId) throws ResourceNotFoundException, BadValuePassedException;

    /**
     * Method returns all the pending rentals for the customer. <br>
     * isApproved - NULL
     *
     * @param username   The customer to get the rental information for
     * @param pageNumber The page number to query the data for
     * @return The object containing the next page token and the pending rentals for the customer.
     */
    HashMap<String, Object> getCustomerPendingRentals(String username, int pageNumber) throws Exception;

    /**
     * Method returns all the rentals that have been approved and can be collected from Banger and Co. <br>
     * isApproved - TRUE & isCollected - FALSE
     *
     * @param username   The customer to get the can be collected rentals to
     * @param pageNumber The page number to get paginated data
     * @return The object containing the list of can be collected vehicles and the next page token.
     */
    HashMap<String, Object> getCustomerCanBeCollectedRentals(String username, Integer pageNumber) throws Exception;

    /**
     * Method will return a list of the rentals of the customer that have been returned (PAST RENTAL HISTORY) <br>
     * isCollected - TRUE & isReturned - TRUE
     *
     * @param username   The customer to get the past rentals for
     * @param pageNumber The page information
     * @return The object containing next page information and the past rentals.
     */
    HashMap<String, Object> getCustomerCompletedRentals(String username, Integer pageNumber) throws Exception;

    /**
     * Method will get a list of on-going rentals for the customer <br>
     * isApproved - TRUE & isCollected - TRUE & isReturned - FALSE
     *
     * @param username   The customer to get the on going rentals for
     * @param pageNumber The page number
     * @return The list of on going rentals
     * @throws Exception Thrown during image de-compression
     */
    HashMap<String, Object> getCustomerOnGoingRentals(String username, Integer pageNumber) throws Exception;

    /**
     * Method will get a list of on-going rentals for the customer.
     * <br> isApproved - FALSE
     *
     * @param username   The customer to get the rejected rentals for
     * @param pageNumber The page number
     * @return The list of rejected rentals along with the next page token.
     */
    HashMap<String, Object> getCustomerRejectedRentals(String username, Integer pageNumber) throws Exception;

    /**
     * Method returns a list of the rejected rentals at Banger and Co.
     *
     * @param pageNumber The page number to get the data for
     * @return The object containing the rejected rentals and the next page number.
     */
    HashMap<String, Object> getAllRejectedRentals(Integer pageNumber) throws Exception;

    /**
     * Method will get a list of all the approved rentals that can be collected from Banger and Co.
     *
     * @param pageNumber The page number to query the data for
     * @return The object consisting of all the approved rentals along with the next page token
     */
    HashMap<String, Object> getAllApprovedRentals(Integer pageNumber) throws Exception;

    /**
     * Method will get a list of all on-going rentals for the given page number
     *
     * @param pageNumber The page number to get the data for
     * @return The list containing the on-going rentals and the next page number.
     */
    HashMap<String, Object> getAllOnGoingRentals(Integer pageNumber) throws Exception;

    /**
     * Method will return a list of all completed/past rentals at Banger and Co.
     *
     * @param pageNumber The page number to get the data for
     * @return The list containing all past rentals and the next page number.
     */
    HashMap<String, Object> getAllCompletedRentals(Integer pageNumber) throws Exception;

    /**
     * Method will start the rental if it exists and has not been started before and if the customer is not blacklisted
     * <br>
     * When collecting - isApproved - true && isCollected - true && isReturned - false
     *
     * @param rentalId The rental to start
     */
    void startRental(Integer rentalId) throws ResourceNotFoundException, BadValuePassedException, ResourceNotUpdatedException;

    /**
     * Method will check if the customer has any on-going, pending, approved rentals for the given time period.
     *
     * @param theCustomer    The customer that is renting
     * @param pickupDateTime The start time period
     * @param returnDateTime The end time period
     */
    void isCustomerHavingPendingOnGoingApprovedRentalsForPeriod(User theCustomer, LocalDateTime pickupDateTime, LocalDateTime returnDateTime) throws ResourceNotCreatedException;

    /**
     * Method will be used to complete the rental only if it collected by the customer.
     *
     * <br>
     * <p>
     * After returning the rental, add the equipment stock back to the original quantity
     *
     * @param rentalId The rental to complete
     */
    void completeRental(Integer rentalId) throws ResourceNotFoundException, ResourceNotUpdatedException;

    List<ChartReturn> getCompletedRentalsForPast12Months() throws Exception;
}
