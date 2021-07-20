package com.lakindu.bangerandcobackend.serviceinterface;

import com.lakindu.bangerandcobackend.dto.VehicleCreateDTO;
import com.lakindu.bangerandcobackend.dto.VehicleRentalFilterDTO;
import com.lakindu.bangerandcobackend.dto.VehicleShowDTO;
import com.lakindu.bangerandcobackend.dto.VehicleUpdateDTO;
import com.lakindu.bangerandcobackend.entity.Vehicle;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceAlreadyExistsException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceCannotBeDeletedException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotFoundException;
import com.lakindu.bangerandcobackend.util.exceptionhandling.customexceptions.ResourceNotUpdatedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.DataFormatException;

public interface VehicleService {
    void createVehicle(VehicleCreateDTO theDTO, MultipartFile vehicleImage) throws ResourceNotFoundException, ResourceAlreadyExistsException, IOException, DataFormatException;

    /**
     * This method will return a list of all the vehicles available at Banger and Co that will be viewed only via an administrator.
     *
     * @return All vehicle information in the database.
     * @throws DataFormatException Thrown by java.util.zip
     * @throws IOException         Thrown by java.util.zip
     */
    List<VehicleShowDTO> getAllVehicles() throws DataFormatException, IOException;

    /**
     * Logic behind checking if vehicle is available for rental:
     * First get the list of rentals for every vehicle.
     * <p>
     * If there are no rentals, it is directly added as a vehicle that is available.
     * <p>
     * If there are rentals for the vehicle: the RENTAL Pickup DATE_TIME and Return DATE_TIME is obtained
     * <p>
     * The filtering Pickup DATE_TIME and Return DATE_TIME is obtained
     * <p>
     * Check is done to see if filtering Pickup DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
     * <p>
     * OR
     * <p>
     * Check is done to see if filtering Return DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
     * <p>
     * IF the Passed PICKUP or RETURN Date_Time is between a RENTAL PICKUP or RETURN DATE_TIME, check if each vehicle for rental is returned, if so vehicle can be rented again
     * <p>
     * IF not, there may be rentals that fall between the PASSED Pickup and Return DATE_TIME, check those rentals.
     * <p>
     * IF rentals fall between Passed PICKUP/RETURN Date_Time, check if they're returned, if so, can be rented again.
     * <p>
     * <b>if customer is less than 25 years old, they can rent only SMALL TOWN CARS as the banger only insured for that age</b
     *
     * @param theFilterDTO The object containing the REQUESTING Pickup DATE_TIME and Return DATE_TIME
     * @param loggedInUser
     * @return The list of vehicles that can be rented between time period.
     * @throws DataFormatException Thrown when dates passed from client is invalid.
     * @throws IOException         Thrown due to image decompression issues.
     * @author Lakindu Hewawasam
     * @since 13th June 2021
     */
    List<VehicleShowDTO> getAllVehiclesThatCanBeRentedForGivenPeriod(VehicleRentalFilterDTO theFilterDTO, Authentication loggedInUser) throws DataFormatException, IOException;

    void removeVehicleById(int vehicleId) throws ResourceNotFoundException, ResourceCannotBeDeletedException;

    /**
     * Method returns a vehicle object for the given ID.
     *
     * @param vehicleId The id to get the vehicle for
     * @return The vehicle information for the given ID queried from the database.
     * @author Lakindu Hewawasam
     */
    VehicleShowDTO getVehicleById(int vehicleId) throws DataFormatException, IOException, ResourceNotFoundException;

    /**
     * Method that holds the validation logic for the rental or for filtering
     * <p>
     * <p>
     * Logic behind checking if vehicle is available for rental:
     * First get the list of rentals for every vehicle.
     * <p>
     * If there are no rentals, it is directly added as a vehicle that is available.
     * <p>
     * If there are rentals for the vehicle: the RENTAL Pickup DATE_TIME and Return DATE_TIME is obtained
     * <p>
     * The filtering Pickup DATE_TIME and Return DATE_TIME is obtained
     * <p>
     * Check is done to see if filtering Pickup DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
     * <p>
     * OR
     * <p>
     * Check is done to see if filtering Return DATE_TIME is between RENTAL Pickup DATE_TIME and Return DATE_TIME
     * <p>
     * IF the Passed PICKUP or RETURN Date_Time is between a RENTAL PICKUP or RETURN DATE_TIME, check if each vehicle for rental is returned, if so vehicle can be rented again
     * <p>
     * IF not, there may be rentals that fall between the PASSED Pickup and Return DATE_TIME, check those rentals.
     * <p>
     * IF rentals fall between Passed PICKUP/RETURN Date_Time, check if they're returned, if so, can be rented again.
     *
     * @param theVehicle     The vehicle to check
     * @param pickupDateTime The customer pickup time
     * @param returnDateTime The customer return time
     * @return The boolean indicating if the vehicle is available or not.
     */
    boolean isVehicleAvailableOnGivenDates(Vehicle theVehicle, LocalDateTime pickupDateTime, LocalDateTime returnDateTime);

    /**
     * Returns the vehicle for the given id.
     *
     * @param vehicleId The vehicle to retrieve
     * @return The vehicle from the database.
     */
    Vehicle _getVehicleInformation(int vehicleId) throws ResourceNotFoundException;

    void checkIfVehicleHasPendingOrOnGoingRentals(Vehicle theVehicleToBeRemoved) throws ResourceCannotBeDeletedException;


    /**
     * Method executed to update a vehicle. The vehicle can be updated only if there are no pending or on-going rentals for the vehicle.
     *
     * @param updateObject The new update information
     * @return The updated vehicle information.
     */
    Vehicle updateVehicle(VehicleUpdateDTO updateObject) throws ResourceNotFoundException, DataFormatException, IOException, ResourceNotUpdatedException, ResourceCannotBeDeletedException;
}
