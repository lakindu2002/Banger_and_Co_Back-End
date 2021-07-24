package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    List<Rental> getAllOnGoingRentals();

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
}

