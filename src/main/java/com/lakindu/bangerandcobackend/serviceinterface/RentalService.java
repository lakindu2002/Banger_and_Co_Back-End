package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.RentalCreateDTO;
import com.lakindu.bangerandcobackend.dto.RentalShowDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotCreatedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
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
}
