package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.entity.User;

import java.io.IOException;
import java.util.HashMap;

public interface LicenseCheckerService {
    /**
     * Method will be used to load the CSV will from the DMV and to check if the customer's license is present.
     * If so, their, rental will be rejected.
     *
     * @param theCustomer The customer trying to make the rental
     * @return The Hashmap containing key: `license status`: VALID, STOLEN, LOST, SUSPENDED
     * @throws IOException Exceptions thrown when reading the file.
     */
    HashMap<String, String> isLicenseSuspendedLostStolen(User theCustomer) throws IOException;
}
