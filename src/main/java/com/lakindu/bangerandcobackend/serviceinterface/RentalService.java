package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.BadValuePassedException;

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
}
