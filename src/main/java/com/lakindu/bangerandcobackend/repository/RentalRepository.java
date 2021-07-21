package com.lakindu.bangerandcobackend.repository;

import com.lakindu.bangerandcobackend.entity.Rental;
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
     *
     * @param isApproved The boolean to indicate that the rental is not approved.
     * @param pageable   The page to implement pagination.
     * @return The list of pending rentals fetched from the database.
     */
    List<Rental> getAllByIsApprovedEquals(Boolean isApproved, Pageable pageable);
}
