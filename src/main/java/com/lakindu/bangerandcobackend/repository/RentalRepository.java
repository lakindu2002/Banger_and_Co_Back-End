package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Integer> {
    @Query("SELECT theRental " +
            "FROM Rental theRental " +
            "WHERE (theRental.returnDate <:currentDate OR theRental.returnDate=:currentDate) " +
            "AND theRental.isApproved=:isApproved " +
            "AND theRental.isCollected=:isCollected")
    List<Rental> getAllRentalsThatHavePassedReturnDate(LocalDate currentDate, Boolean isApproved, Boolean isCollected);

    /**
     * Retrieve a list of the pending rentals.
     * All pending rentals is where the isApproved attribute of the Rental is at NULL.
     *
     * @param pageable The page to implement pagination.
     * @return The list of pending rentals fetched from the database.
     */
    @Query("FROM Rental theRental WHERE theRental.isApproved is NULL")
    List<Rental> getAllByIsApprovedEquals(Pageable pageable);

    /**
     * Method will return a list of all pending rentals for the customer
     *
     * @param isApproved  NULL
     * @param theCustomer The customer to get the data for
     * @param pageable    The pagination information
     * @return The data from the database.
     */
    List<Rental> getAllByIsApprovedEqualsAndTheCustomerRentingEquals(Boolean isApproved, User theCustomer, Pageable pageable);

    /**
     * Method will return a list of the vehicles that can be collected for a particular user
     *
     * @param isApproved  TRUE - RENTAL APPROVED
     * @param isCollected FALSE - NOT YET COLLECTED
     * @param theCustomer The customer to get the data from.
     * @param pageable    The page information
     * @return The information from the database
     */
    List<Rental> getAllByIsApprovedEqualsAndIsCollectedEqualsAndTheCustomerRentingEquals(Boolean isApproved, Boolean isCollected, User theCustomer, Pageable pageable);

    /**
     * Method will get a list of all completed rentals by the customer
     *
     * @param isApproved  TRUE
     * @param isCollected TRUE
     * @param isReturned  TRUE
     * @param theCustomer The customer renting the vehicle
     * @param pageable    The next page
     * @return The database return
     */
    List<Rental> getAllByIsApprovedEqualsAndIsCollectedEqualsAndIsReturnedEqualsAndTheCustomerRentingEquals(Boolean isApproved, Boolean isCollected, Boolean isReturned, User theCustomer, Pageable pageable);

    /**
     * Method will return a list of all rejected rentals at Banger and Co. <br>
     * Rejected - isApproved FALSE
     *
     * @param theNextPage The page to get the data for
     * @return The rental list for the given page.
     */
    @Query("FROM Rental theRental WHERE theRental.isApproved=false")
    List<Rental> getAllRejectedRentals(Pageable theNextPage);

    /**
     * Method will get a list of all vehicles that can be collected from Banger and Co.
     *
     * @param isApproved  - TRUE
     * @param isCollected - FALSE
     * @param nextPage    Page to get data for
     * @return Rental list for given page.
     */
    @Query("FROM Rental theRental WHERE theRental.isApproved=:isApproved AND theRental.isCollected=:isCollected")
    List<Rental> getAllCanBeCollectedRentals(boolean isApproved, boolean isCollected, Pageable nextPage);

    /**
     * Method will get a list of all on-going rentals at Banger and Co.
     * <br> For a rental to be on-going
     * <ul>
     *     <li>IsApproved = true</li>
     *     <li>IsCollected = true</li>
     *     <li>IsReturned = false</li>
     * </ul>
     * <br>Means that the rental has been collected but not yet returned - hence, on-going.
     *
     * @return
     */
    @Query("FROM Rental theRental WHERE theRental.isApproved=true AND theRental.isCollected=true AND theRental.isReturned=false")
    List<Rental> getAllOnGoingRentals(Pageable pageRequest);

    /**
     * Method will return a list of all completed rentals at Banger and Co.
     * <br> For a rental to be completed
     * <ul>
     *     <li>isApproved - true</li>
     *     <li>isCollected - true</li>
     *     <li>isReturned - true</li>
     * </ul>
     *
     * @param theNextPage The page to get the results for
     * @return The completed rentals from the database.
     */
    @Query("FROM Rental theRental WHERE theRental.isApproved=true AND theRental.isCollected=true AND theRental.isReturned=true")
    List<Rental> getAllCompletedRentals(Pageable theNextPage);

    /**
     * Get All the rentals for the given customer
     *
     * @param theCustomer The customer to get the rentals for
     * @return The database result containing the rentals for the customer
     */
    List<Rental> getAllByTheCustomerRentingEquals(User theCustomer);

    /**
     * Method will return the completed rentals for the past 12 months
     *
     * @param startDate The start date = current date - 12 months
     * @return The list of rentals over the past 12 months
     */
    @Query(
            "FROM Rental theRental WHERE " +
                    "(theRental.isCollected=true AND theRental.isReturned=true) AND " +
                    "(theRental.pickupDate>=:startDate)"
    )
    List<Rental> getAllCompletedRentalsForPast12Months(LocalDate startDate);

    /**
     * Method will return a list of vehicles that needs to be collected within the given month
     * <br>
     * Criteria - PICKUP DATE >= 1 and PICKUP DATE <= LAST DAY OF MONTH
     *
     * @param isApproved  Indicate rental being approved
     * @param isCollected Should be false
     * @param startDate   First date of the month
     * @param endDate     Last date of the month
     * @return Rentals that can be collected for given month
     */
    List<Rental> getAllByIsApprovedEqualsAndIsCollectedEqualsAndPickupDateGreaterThanEqualAndPickupDateLessThanEqual(boolean isApproved, boolean isCollected, LocalDate startDate, LocalDate endDate);

    /**
     * Method used to get all the pending rentals for the statistics panel for the admin
     *
     * @param isApproved = NULL
     * @return The pending rentals
     */
    List<Rental> findAllByIsApprovedEquals(Boolean isApproved);

    /**
     * Method used to get all on-going rentals for the statistics panel for the admin
     *
     * @param isApproved  TRUE
     * @param isCollected TRUE
     * @param isReturned  FALSE
     * @return All on-going rentals in the DB
     */
    List<Rental> findAllByIsApprovedEqualsAndIsCollectedEqualsAndIsReturnedEquals(
            Boolean isApproved, Boolean isCollected, Boolean isReturned
    );

    @Query(
            "FROM Rental theRental WHERE theRental.pickupDate >=:updatingReturnDate"
    )
    List<Rental> findRentalsFromToday(LocalDate updatingReturnDate);

    /**
     * A stored procedure that, once called, will be used communicate with the Insurer Database to check if the passed license number has been associated with any fraudulent claims.
     * <br>
     * <br>
     * The license number passed will be used to fetch data through an `SQL VIEW` and inside the stored procedure, the returned VIEW query will be counted.
     * <br>
     * <br>
     * IF COUNT > 0: <b>FRAUD</b>, ELSE: <b>CLEAN</b>
     * <br>
     *
     * @param licenseNumber The license number to check against the insurer database
     * @return Status - Can only be <i>(case-sensitive)</i>: <b>FRAUD</b> or <b>CLEAN</b>
     * @author Lakindu Hewawasam
     */
    @Query(nativeQuery = true, value = "CALL IS_USER_FRADULENT(:licenseNumber)")
    List<HashMap<String, Object>> isCustomerLicenseFraudulent(String licenseNumber);
}

