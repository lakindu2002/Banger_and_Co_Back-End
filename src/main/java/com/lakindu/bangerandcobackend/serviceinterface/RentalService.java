package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.ChartReturn;
import com.lakindu.bangerandcobackend.dto.RentalCreateDTO;
import com.lakindu.bangerandcobackend.dto.RentalShowDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.entity.User;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotCreatedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;
import org.springframework.security.core.Authentication;

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

    /**
     * Method used to create a rental at Banger and Co.
     *
     * @param theRental The rental to be made
     * @throws ParseException              Thrown during date time conversion
     * @throws BadValuePassedException     Thrown when user sends invalid data
     * @throws ResourceNotFoundException   Thrown when the vehicle to be rented cannot be found
     * @throws ResourceNotCreatedException Thrown when the rental is not made due to an exception in the flow.
     */
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
    void startRental(Integer rentalId) throws Exception;

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

    /**
     * Method will get a list of all completed rentals in the past 12 months.
     * <br>
     * Criteria: If the rental is made in the current month and finishes during next month, the rental will be counted as completed for current month as it was made in current month
     *
     * @return The chart object containing chart data
     * @throws Exception Thrown during iterating over rental list
     */
    List<ChartReturn> getCompletedRentalsForPast12Months() throws Exception;

    /**
     * Method will get all money made (profit) for past 12 months.
     * <br>
     * Criteria: If the rental is made in the current month and finishes during next month, the rental will be counted as completed for current month as it was made in current month
     *
     * @return The chart object containing chart data
     * @throws Exception Thrown during iterating over rental list
     */
    List<ChartReturn> getProfitsForLast12Months() throws Exception;

    /**
     * Method will get a list of all vehicles that need to be collected for current month.
     * <br>
     * Min Date - 1st
     * Max Date - Last date of month
     * <br>
     * Criteria: Pickup date falls between first and last date of the month
     *
     * @return The list of rentals needed to be collected within the month
     * @throws Exception The exception thrown when creating the DTO List
     */
    List<RentalShowDTO> getVehiclesToBeCollectedForMonth() throws Exception;

    /**
     * Method will return a list of all pending rentals
     *
     * @return All Pending Rentals
     * @throws Exception The exception thrown when creating the DTO List
     */
    List<RentalShowDTO> getAllPendingRentalsForStatistics() throws Exception;

    /**
     * Method will return a list of all on-going rentals
     *
     * @return All On-Going Rentals
     * @throws Exception The exception thrown when creating the DTO List
     */
    List<RentalShowDTO> getAllOnGoingRentalsForChart() throws Exception;

    /**
     * Method will create a late return for the rental
     * <br>
     * The rental can be late returned only if the customer is a returning customer and if the rental has been collected and not returned.
     *
     * @param rentalId The rental for late request
     */
    void createLateReturnForRental(Integer rentalId, Authentication loggedInUser) throws ResourceNotFoundException, ResourceNotUpdatedException, BadValuePassedException;

    /**
     * Method will cancel a late return for a rental
     * <br>
     * In order to cancel a late return the rental must be on late return in the first place
     *
     * @param rentalId     The rental to cancel the late return for
     * @param loggedInUser The user logged in to the system == to the customer on rental being cancelled for late return
     */
    void cancelLateReturn(Integer rentalId, Authentication loggedInUser) throws ResourceNotFoundException, ResourceNotUpdatedException;

    /**
     * Method will get customer on-going rentals without any pagination.
     *
     * @param name The customer username
     */
    List<RentalShowDTO> getCustomerOnGoingRentals(String name) throws Exception;

    HashMap<String, Integer> countCustomerPendingRentals(String username) throws DataFormatException, IOException, ResourceNotFoundException;

    HashMap<String, Integer> countCustomerPastRentals(String username) throws DataFormatException, IOException, ResourceNotFoundException;

    HashMap<String, Integer> countCustomerRejectedRentals(String username) throws DataFormatException, IOException, ResourceNotFoundException;

}
